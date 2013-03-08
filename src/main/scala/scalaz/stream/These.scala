package scalaz.stream

import scalaz.{Monad, Monoid}

trait These[+A,+B] {
  import These._

  def flip: These[B,A] = this match {
    case This(x) => That(x)
    case That(x) => This(x)
    case Both(x,y) => Both(y,x)
  }

  def mapThis[A2](f: A => A2): These[A2,B] = this match {
    case This(a) => This(f(a)) 
    case Both(a,b) => Both(f(a),b) 
    case t@That(_) => t
  }

  def mapThat[B2](f: B => B2): These[A,B2] = this match {
    case That(b) => That(f(b)) 
    case Both(a,b) => Both(a, f(b)) 
    case t@This(_) => t
  }
}

object These {
  case class This[+X](left: X) extends These[X, Nothing]
  case class That[+Y](right: Y) extends These[Nothing, Y]
  case class Both[+X,+Y](left: X, right: Y) extends These[X, Y]
  
  implicit def theseInstance[X](implicit X: Monoid[X]) = 
  new Monad[({type f[y] = These[X,y]})#f] {
    def point[Y](x: => Y): These[X,Y] = That(x) 
    def bind[Y,Y2](t: These[X,Y])(f: Y => These[X,Y2]): These[X,Y2] = 
      t match {
        case a@This(_) => a
        case That(x) => f(x)
        case Both(x,y) => f(y).mapThis(x2 => X.append(x,x2)) 
      }
  }
  import scalaz.syntax.{ApplyOps, ApplicativeOps, FunctorOps, MonadOps}
  
  trait TheseT[X] { type f[y] = These[X,y] }

  implicit def toMonadOps[X:Monoid,A](f: These[X,A]): MonadOps[TheseT[X]#f,A] = 
    theseInstance.monadSyntax.ToMonadOps(f)
  implicit def toApplicativeOps[X:Monoid,A](f: These[X,A]): ApplicativeOps[TheseT[X]#f,A] = 
    theseInstance.applicativeSyntax.ToApplicativeOps(f)
  implicit def toApplyOps[X:Monoid,A](f: These[X,A]): ApplyOps[TheseT[X]#f,A] = 
    theseInstance.applySyntax.ToApplyOps(f)
  implicit def toFunctorOps[X:Monoid,A](f: These[X,A]): FunctorOps[TheseT[X]#f,A] =
    theseInstance.functorSyntax.ToFunctorOps(f)
}
