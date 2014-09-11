package eu.inn.binders.cassandra

trait IfApplied[+A] extends Product with Serializable {
  self =>

  def isApplied: Boolean

  def get: A

  def getOrElse[B >: A](default: => B): B =
    if (isApplied) default else this.get

  def map[B](f: A => B): Option[B] =
    if (isApplied) None else Some(f(this.get))

  def flatMap[B](f: A => Option[B]): Option[B] =
    if (isApplied) None else f(this.get)

  def iterator: Iterator[A] =
    if (isApplied) collection.Iterator.empty else collection.Iterator.single(this.get)

  def toList: List[A] =
    if (isApplied) List() else new ::(this.get, Nil)
}

case class NotApplied[+A](x: A) extends IfApplied[A] {
  def isApplied = false
  def get = x
}

case object Applied extends IfApplied[Nothing] {
  def isApplied = true
  def get = throw new NoSuchElementException("Applied.get")
}
