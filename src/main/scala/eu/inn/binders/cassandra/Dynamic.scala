package eu.inn.binders.cassandra

case class Dynamic(query: String) extends DynamicQuery {
  override def getDynamicQuery: String = query
}
