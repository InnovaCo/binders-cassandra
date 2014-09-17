package eu.inn.binders.cassandra

trait IfApplied[+A] extends Product with Serializable {
  self =>

  def isApplied: Boolean

  def isExists: Boolean

  def result: Option[A]
}

case class NotAppliedExists[+A](x: A) extends IfApplied[A] {
  def isApplied = false
  def isExists = true
  def result = Some(x)
}

case object NotApplied extends IfApplied[Nothing] {
  def isApplied = false
  def isExists = false
  def result = None
}

case object Applied extends IfApplied[Nothing] {
  def isApplied = true
  def isExists = true
  def result = None
}
