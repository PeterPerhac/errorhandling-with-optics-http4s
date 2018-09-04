# Error handling within an http4s Web application

Modeling errors via a sealed trait and having exhaustive pattern matching in error handler associated with an http4s service, without EitherT - just using MonadError and an optics library that derives MonadError instances

Took me a while to get all the moving parts together into a working example. The code is largely based (with minor modifications) on Gabriel Volpe's article here: https://typelevel.org/blog/2018/08/25/http4s-error-handling-mtl.html and this Gist: https://gist.github.com/gvolpe/3fa32dd1b6abce2a5466efbf0eca9e94


date first implemented: 2018-09-04

