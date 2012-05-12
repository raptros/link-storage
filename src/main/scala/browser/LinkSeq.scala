package local.nodens.linkstorage.browser
import local.nodens.linkmodel._
import local.nodens.linkstorage.{LinkItem, SeparatorItem}
import android.os.Bundle
import android.app._
import android.view._
import android.widget._
import android.content.Context
import scala.collection.JavaConversions._

trait LinkSeqBrowserListener { def onLinkSeqItemSelect(seq:Int, pos:Int, path:List[String]):Unit }

/**
 * Link sequence browser. 
 * Implementation: the links of each link sequence are displayed, with separators between each sequence.
 */
class LinkSeqBrowser extends BrowserFragment {
  var listener:Option[LinkSeqBrowserListener] = None
  var linkSeqs:List[List[(String, String)]] = Nil

  override def onAttach(activity:Activity) = {
    super.onAttach(activity)
    listener = {
      if (activity.isInstanceOf[LinkSeqBrowserListener]) 
        Some(activity.asInstanceOf[LinkSeqBrowserListener]) 
      else None
    }
  }
  
  override def extractArgs(args:Bundle) = {
    super.extractArgs(args)
    linkSeqs = try {
      (args getParcelableArrayList kLinkSeqBundles) map {
        (bundle:Bundle) => {
          val titles = extractStringList(bundle, kLinkTitles)
          val urls = extractStringList(bundle, kLinkUrls)
          titles zip urls
        }
      } toList
    } 
    catch { case e:NullPointerException => Nil }
  }
  
  override def onCreate(sis:Bundle) = {
    super.onCreate(sis)
    setListAdapter(new LinkSeqAdapter(getActivity, linkSeqs))
  }

  override def onListItemClick(lv:ListView, v:View, pos:Int, id:Long) = getListAdapter
  .asInstanceOf[LinkSeqAdapter].lookup(pos) match {
    case Right((i, j)) => listener foreach (_.onLinkSeqItemSelect(i, j, path))
    case Left(_) => ()
  }
}

object LinkSeqBrowser extends BrowserFragmentMaker {
  /**
   * Creates a bundle for a link sequence.
   * @param idx The index of the target link sequence; unchecked.
   * @param section The section containing the link sequence.
   * @return A bundle containing the link sequence in the fashion 
   * as bundles for LinkBrowser.
   */
  private def extractBundleForSeq(idx:Int, section:Section):Bundle = {
    val bundle = new Bundle
    val ls = section.linkSeqs(idx)
    bundle.putStringArrayList(kLinkTitles, arrayList(ls.links map (_.title)))
    bundle.putStringArrayList(kLinkUrls, arrayList(ls.links map (_.url)))
    bundle
  }

  def apply(section:Section, path:List[String]) = fixFragment(new LinkSeqBrowser, path) {
    bundle => {
      val count = section.linkSeqs.length
      val bundles = (0 until count) map {
        idx => extractBundleForSeq(idx, section)
      }
      bundle.putParcelableArrayList(kLinkSeqBundles, arrayList(bundles))
    }
  }
}

/**
 * Adapter for displaying link sequences.
 */
class LinkSeqAdapter(val context:Context, val linkSeqs:List[List[(String, String)]]) extends BaseAdapter {
  val seqArray = linkSeqs map (_ toArray) toArray

  val lookup:Array[Either[Int,(Int, Int)]] = linkSeqs.zipWithIndex
  .map { 
    pair => {
      val (inner, i) = pair
      Left(i)::(inner.indices map (j => Right(i -> j))).toList
    }
  }.flatten.toArray

  def getTitleAt(pos:Int):String = lookup(pos) match {
    case Left(idx) => idx.toString
    case Right((i, j)) => seqArray(i)(j)._1
  }

  def getView(pos:Int, convert:View, parent:ViewGroup):View = lookup(pos) match {
    case Left(idx) =>  convert match {
      case (si:SeparatorItem) => si.update(idx)
      case _ => SeparatorItem(context, idx)
    }
    case Right((i, j)) => convert match {
      case (li:LinkItem) => { li.update(_,_) }.tupled(seqArray(i)(j))
      case _ => { (t:String, u:String) => LinkItem(context, t, u)}
      .tupled(seqArray(i)(j))
    }
  }
  def getItemId(pos:Int):Long = pos.longValue()
  def getItem(pos:Int):Object = lookup(pos) match {
    case Left(idx) => idx:java.lang.Integer
    case Right((i, j)) => seqArray(i)(j)
  }
  def getCount:Int = lookup.length
  
  override val areAllItemsEnabled = false
  override val getViewTypeCount = 2

  override def isEnabled(pos:Int) = lookup(pos) match {
    case Left(_) => false
    case Right(_) => true
  }

}
