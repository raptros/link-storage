package local.nodens.linkstorage.browser
import local.nodens.linkmodel._
import local.nodens.linkstorage.SectionItem

import android.view._
import android.widget._
import android.app._
import android.os.Bundle
import android.content.Context

trait SectionBrowserListener { def onSectionSelect(secName:String, path:List[String]):Unit }

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
    setListAdapter(new SectionAdapter(getActivity, sections))
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
 * Adapter for displaying section lists.
 */
class SectionAdapter(val context:Context, sections:List[String]) extends BaseAdapter {
  val keyArray = sections.toArray

  def getView(pos:Int, convert:View, parent:ViewGroup):View = convert match {
    case si:SectionItem => si.update(keyArray(pos))
    case _ => SectionItem(context, keyArray(pos))
  }
  def getItemId(pos:Int):Long = pos.longValue()
  def getItem(pos:Int):Object = keyArray(pos)
  def getCount:Int = keyArray.length
}

