package local.nodens.linkstorage

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MMap, Buffer}

import java.util.{ArrayList => JArrayList}

import android.app._
import android.content._
import android.os.Bundle
import android.view._
import android.widget._
import android.os.Bundle
import android.util.Log

import local.nodens.linkmodel._

/**
 * Trait for important fragment keys and various helpful functions.
 */
trait BrowserUtils  {
  val kSections = "sections"
  val kLinkTitles = "link_titles"
  val kLinkUrls = "link_urls"
  val kLinkSeqCount = "link_seq_count"
  val kPath = "path"

  def arrayList[A](iter:Iterable[A]):JArrayList[A] = new JArrayList[A](iter)
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
 * Trait for abstract listenability.
 */
trait ListenableBrowser {
  type Listener
  var listener:Option[Listener] = None
  def applyRef(r:Listener) = listener = Some(r)
  def notify(lv:ListView, v:View, pos:Int, id:Long)(listener:Listener):Unit
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
  def extractArgs(args: Bundle):Unit = {
    path = extractStringList(args, kPath)
  }
  //override def onListItemClick(lv:ListView, v:View, pos:Int, id:Long) = listener foreach (notify(lv,v,pos,id)(_))
}

trait SectionBrowserListener { def onSectionSelect(secName:String, path:List[String]):Unit }
trait LinkBrowserListener { def onLinkSelect(pos:Int, path:List[String]):Unit }

/**
 * Browse sections below this node.
 */
class SectionBrowser extends BrowserFragment {
  var listener:Option[SectionBrowserListener] = None
  var sections:List[String] = Nil
  override def onAttach(activity:Activity) = {
    super.onAttach(activity)
    listener = if (activity.isInstanceOf[SectionBrowserListener]) Some(activity.asInstanceOf[SectionBrowserListener]) else None
  }
  override def extractArgs(args:Bundle) = {
    super.extractArgs(args)
    sections = extractStringList(args, kSections)
  }
  override def onCreate(sis:Bundle) = {
    super.onCreate(sis)
    setListAdapter(new SectionAdapter(sections, getActivity))
  }
  override def onListItemClick(lv:ListView, v:View, pos:Int, id:Long) = listener foreach (_.onSectionSelect(
    getListAdapter.getItem(pos).asInstanceOf[String], path))
}

/**
 * Construct a section browser for a section or a document.
 */
object SectionBrowser extends BrowserFragmentMaker {
  def apply(section:Section, path:List[String]) = fixFragment(new SectionBrowser, path)(_ putStringArrayList(
    kSections, arrayList(section.sections.keys)))
  def apply(document:Document) = fixFragment(new SectionBrowser, Nil)(_ putStringArrayList(
    kSections, arrayList(document.sections.keys)))
}

/**
 * Browse links.
 */
class LinkBrowser extends BrowserFragment {
  var listener:Option[LinkBrowserListener] = None
  var titles:List[String] = Nil
  var urls:List[String] = Nil
  override def onAttach(activity:Activity) = {
    super.onAttach(activity)
    listener = if (activity.isInstanceOf[LinkBrowserListener]) Some(activity.asInstanceOf[LinkBrowserListener]) else None
  }
  override def extractArgs(args:Bundle) = {
    super.extractArgs(args)
    titles = extractStringList(args, kLinkTitles)
    urls = extractStringList(args, kLinkUrls)
  }
  override def onCreate(sis:Bundle) = {
    super.onCreate(sis)
    setListAdapter(new LinkAdapter(titles, urls, getActivity))
  }
  override def onListItemClick(lv:ListView, v:View, pos:Int, id:Long) = listener foreach (_.onLinkSelect(pos, path))
}

/**
 * Prepare a link browser.
 */
object LinkBrowser extends BrowserFragmentMaker {
  def apply(section:Section, path:List[String]) = fixFragment(new LinkBrowser, path) {
    args => {
      args putStringArrayList (kLinkTitles, arrayList(section.links map (_.title)))
      args putStringArrayList(kLinkUrls, arrayList(section.links.map (_.url)))
    }
  }
}

// take a look at this for prepping the whatsit...
class LinkSeqBrowser extends ListFragment {
  /*override def onCreateView(inflater:LayoutInflater, container:ViewGroup, savedInstanceState:Bundle) = {
    
  }*/
}

object LinkSeqBrowser extends BrowserFragmentMaker {
  def apply(section:Section, path:List[String]) = fixFragment(new LinkSeqBrowser, path)(_ putInt (kLinkSeqCount,section.linkSeqs.length))
}
