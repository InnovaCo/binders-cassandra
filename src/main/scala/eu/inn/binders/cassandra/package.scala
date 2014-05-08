package eu.inn.binders

import com.datastax.driver.core.{Cluster, Session}

package object cassandra {
  implicit class SessionHelper(val session: Session) {
    def safeClose(): Unit = {
      if (session != null)
        session.close()
    }
  }

  implicit class ClusterHelper(val cluster: Cluster) {
    def safeClose(): Unit = {
      if (cluster != null)
        cluster.close()
    }
  }
}
