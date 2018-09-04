package uk.co.devproltd.errorhandling

import cats.data.{Kleisli, OptionT}
import cats.effect._
import cats.effect.concurrent.Ref
import cats.syntax.all._
import cats.{ApplicativeError, MonadError}
import fs2.StreamApp
import fs2.StreamApp.ExitCode
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.higherKinds

//https://typelevel.org/blog/2018/08/25/http4s-error-handling-mtl.html

case class User(username: String, age: Int)
case class UserList(users: List[User])
case class UserUpdateAge(age: Int)

sealed trait UserError extends Exception
case class UserAlreadyExists(username: String) extends UserError
case class UserNotFound(username: String) extends UserError
case class InvalidUserAge(age: Int) extends UserError

trait UserAlgebra[F[_]] {
  def list: F[UserList]
  def find(username: String): F[Option[User]]
  def save(user: User): F[Unit]
  def updateAge(username: String, age: Int): F[Unit]
}

object UserInterpreter {

  def create[F[_]](implicit F: Sync[F]): UserAlgebra[F] =
    new UserAlgebra[F] {
      private val state = Ref.unsafe[F, Map[String, User]](Map.empty)
      private def validateAge(age: Int): F[Unit] =
        if (age <= 0) F.raiseError(InvalidUserAge(age)) else F.unit

      override def list: F[UserList] =
        state.get.map(m => UserList(m.values.toList))

      override def find(username: String): F[Option[User]] =
        state.get.map(_.get(username))

      override def save(user: User): F[Unit] = {
        val createIfNotExists: Option[User] => F[Unit] =
          _.fold(state.update(_.updated(user.username, user)))(u => F.raiseError(UserAlreadyExists(u.username)))

        validateAge(user.age) *> find(user.username) >>= createIfNotExists
      }

      override def updateAge(uname: String, age: Int): F[Unit] = {
        val updateAgeIfExists: Option[User] => F[Unit] =
          _.fold(F.raiseError[Unit](UserNotFound(uname)))(u => state.update(_.updated(uname, u.copy(age = age))))

        validateAge(age) *> find(uname) >>= updateAgeIfExists
      }
    }

}

trait HttpErrorHandler[F[_], E <: Throwable] {
  def handle(routes: HttpRoutes[F]): HttpRoutes[F]
}

object RoutesHttpErrorHandler {
  def apply[F[_]: ApplicativeError[?[_], E], E <: Throwable](routes: HttpRoutes[F])(
    handler: E => F[Response[F]]): HttpRoutes[F] =
    Kleisli(req => OptionT(routes.run(req).value.handleErrorWith(e => handler(e).map(Option.apply))))
}

class UserRoutesMTL[F[_]](userAlgebra: UserAlgebra[F])(implicit F: Sync[F], H: HttpErrorHandler[F, UserError])
    extends Http4sDsl[F] {

  private def okJson[T: Encoder](t: T): F[Response[F]] =
    Ok(t.asJson)

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "users" =>
      userAlgebra.list >>= okJson[UserList]

    case GET -> Root / "users" / username =>
      userAlgebra.find(username) >>= (_.fold(F.raiseError[Response[F]](UserNotFound(username)))(okJson))

    case req @ POST -> Root / "users" =>
      req.as[User] >>= (user => userAlgebra.save(user) *> Created(user.username.asJson))

    case req @ PUT -> Root / "users" / username =>
      req.as[UserUpdateAge] >>= (uu => userAlgebra.updateAge(username, uu.age) *> Created(username.asJson))
  }

  val routes: HttpRoutes[F] = H.handle(httpRoutes)

}

class UserHttpErrorHandler[F[_]: MonadError[?[_], UserError]] extends HttpErrorHandler[F, UserError] with Http4sDsl[F] {

  private val handler: UserError => F[Response[F]] = {
    case InvalidUserAge(age)         => BadRequest(s"Invalid age $age".asJson)
    case UserAlreadyExists(username) => Conflict(s"Username $username already exists!".asJson)
    case UserNotFound(username)      => NotFound(s"User not found: $username".asJson)
  }

  override def handle(routes: HttpRoutes[F]): HttpRoutes[F] = RoutesHttpErrorHandler(routes)(handler)

}

object Server extends StreamApp[IO] {

  import com.olegpy.meow.hierarchy._

  import scala.concurrent.ExecutionContext.Implicits.global

  override def stream(args: List[String], onShutdown: IO[Unit]): fs2.Stream[IO, ExitCode] = app[IO]

  def app[F[_]: ConcurrentEffect]: fs2.Stream[F, ExitCode] = {
    implicit def userHttpErrorHandler: HttpErrorHandler[F, UserError] = new UserHttpErrorHandler[F]

    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(new UserRoutesMTL[F](UserInterpreter.create[F]).routes, "/")
      .serve
  }

}
