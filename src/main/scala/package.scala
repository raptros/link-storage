package local.nodens

package object linkstorage {
  import scalaz._
  import Scalaz._
  import local.nodens.linkmodel.Document

  val TAG = "LinkStorageActivity"

  val kSISDocType = "sis_doc_type"
  val kSISDocFile = "sis_doc_file"
  val kSISDocID = "sis_doc_id"
  val kSISPath = "sis_path"

  val SIS_DOC_TYPE_STATIC:String = "sis_doc_type_static"
  val SIS_DOC_TYPE_EXTERN:String = "sis_doc_type_extern"

  type LoadFail = String
  type Loaded = Validation[LoadFail, Document]
  type OnLoad = Document => Unit
  type OnLoadFail = LoadFail => Unit

  import android.os.Bundle
  import scala.collection.JavaConversions._
  class StringListBundle(bundle:Bundle) {
    import java.util.{ArrayList => JArrayList}
    //extract as string list
    def extractStringList(key:String):List[String] = try {
      (bundle getStringArrayList key).toList
    } catch {
      case e:NullPointerException => Nil
    }
    //put a string iterable in (as an arraylist)
    def putStringList(key:String, iter:Iterable[String]):Unit = {
      bundle.putStringArrayList(key, new JArrayList[String](iter))
    }
  }
  implicit def bundle2StringListBundle(bundle:Bundle):StringListBundle = new StringListBundle(bundle)
}
