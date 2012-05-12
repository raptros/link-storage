package local.nodens.linkstorage

package object browser {
  import java.util.{ArrayList => JArrayList}
  import scala.collection.JavaConversions._
  import scala.collection.mutable.{Map => MMap, Buffer}
  import android.os.Bundle
  import android.util.Log
  import android.app._

  /**
  * Trait for important fragment keys and various helpful functions.
  */
  trait BrowserUtils  {
    val kSections = "sections"
    val kLinkTitles = "link_titles"
    val kLinkUrls = "link_urls"
    val kPath = "path"
    val kLinkSeqBundles = "link_seq_bundles"

    //convenience method for making java array lists
    def arrayList[A](iter:Iterable[A]):JArrayList[A] = new JArrayList[A](iter)

    /**
    * extract a list of strings from a Bundle.
    * @param sis A bundle to extract from
    * @param key The key to attempt to pull from sis
    * @return A List containing whatever was successfully extracted.
    */
    def extractStringList(sis:Bundle, key:String):List[String] =  try {
      (sis getStringArrayList key).toList 
    } catch {
      case e:NullPointerException => Nil
    }
  }

  /**
  * Trait for fragment companion objects.
  */
  trait BrowserFragmentMaker extends BrowserUtils {
    def fixFragment(frag:Fragment, path:List[String])(f: Bundle => Unit):Fragment = {
      val args = new Bundle
      f(args)
      args.putStringArrayList(kPath, arrayList(path))
      Log.d(TAG, args.toString)
      frag.setArguments(args)
      frag
    }
  }

  /**
  * Abstract base class for browser fragments.
  */
  abstract class BrowserFragment extends ListFragment /*with ListenableBrowser*/ with BrowserUtils {
    var path:List[String] = Nil
    override def onCreate(sis:Bundle) = {
      super.onCreate(sis)
      (if (getArguments == null) None else Some(getArguments)) foreach (extractArgs(_))
    }

    def extractArgs(args: Bundle):Unit =  path = extractStringList(args, kPath)
  }
}

