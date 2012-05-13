package local.nodens

package object linkstorage {
  val TAG = "LinkStorageActivity"

  import scalaz._
  import Scalaz._
  import local.nodens.linkmodel.Document

  type Loaded = Validation[String, Document]
  def nullToOption[A](nullable:A):Option[A] = if (nullable == null) None else Some(nullable)
}
