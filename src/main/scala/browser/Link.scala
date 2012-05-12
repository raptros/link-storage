package local.nodens.linkstorage.browser
import local.nodens.linkstorage.LinkItem
import local.nodens.linkmodel._
import android.os.Bundle
import android.app._
import android.view._
import android.widget._
import android.content.Context

trait LinkBrowserListener { def onLinkSelect(pos:Int, path:List[String]):Unit }

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
    setListAdapter(new LinkAdapter(getActivity, titles, urls))
  }
  override def onListItemClick(lv:ListView, v:View, pos:Int, id:Long) = listener foreach (_.onLinkSelect(pos, path))
}


/**
 * Prepare a link browser.
 */
object LinkBrowser extends BrowserFragmentMaker {
  def apply(section:Section, path:List[String]) = fixFragment(new LinkBrowser, path) {
    args => {
      args.putStringArrayList(kLinkTitles, 
        arrayList(section.links map (_.title)))
      args.putStringArrayList(kLinkUrls, 
        arrayList(section.links.map (_.url)))
    }
  }
}

/**
 * Adapter for displaying links.
 */
class LinkAdapter(val context:Context, titles:List[String], urls:List[String]) extends BaseAdapter {
  val titleArr = titles.toArray
  val urlArr = urls.toArray

  def getView(pos:Int, convert:View, parent:ViewGroup):View = convert match {
    case (li:LinkItem) => li.update(titleArr(pos), urlArr(pos))
    case _ => LinkItem(context, titleArr(pos), urlArr(pos))
  }
  def getItemId(pos:Int):Long = pos.longValue()
  def getItem(pos:Int):Object = titleArr(pos)
  def getCount:Int = titleArr.length
}

