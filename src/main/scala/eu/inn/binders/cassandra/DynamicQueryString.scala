package eu.inn.binders.cassandra

class DynamicQueryString(query: String) extends DynamicQuery {
  override def getDynamicQuery: String = query
}
