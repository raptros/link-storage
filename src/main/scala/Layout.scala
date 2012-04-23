package local.nodens.linkstorage

import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.os.Bundle
import android.util.Log
import local.nodens.linkmodel._
/**
 * When one of these is created, it will set up the layout of the activity.
 * @todo implement callbacks for section browser fragment to push to new sections.
 */
abstract class LayoutMgr(val activity:LinkStorageActivity) 
extends Fragment {
  val actionBar = activity.getActionBar
  import ActionBar.{
    NAVIGATION_MODE_TABS,
    NAVIGATION_MODE_STANDARD,
    DISPLAY_SHOW_HOME,
    DISPLAY_HOME_AS_UP
  }
  def setup(doc:Document, path:List[String]):Unit = {
    actionBar.setHomeButtonEnabled(true)
  }

  def tearDown:Unit

}

class TabbedSection(activity:LinkStorageActivity) extends LayoutMgr(activity) {
  import ActionBar.{Tab, TabListener}

  class TabFragListener(fragInit: => Fragment) extends TabListener {
    val content = android.R.id.content
    var mFragment:Option[Fragment] = None
    var added = false
    def onTabReselected(tab:Tab, ft:FragmentTransaction) = {/* do nothing */}
    def onTabSelected(tab:Tab, ft:FragmentTransaction) =  {
      val fragCheck = (mFragment orElse { added = false; Some(fragInit)}).get
      mFragment = if (fragCheck == null) {
        added = false; Some(fragInit)
      } else Some(fragCheck)
      mFragment map {
        frag => if (added) ft.attach(frag) 
        else {
          ft.add(content, frag)
          added = true
        }
      }
    }
    def onTabUnselected(tab:Tab, ft:FragmentTransaction) = mFragment map (ft.detach(_))
  }

  def tearDown {
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    actionBar.setDisplayHomeAsUpEnabled(false)
    actionBar.removeAllTabs
  }

  override def setup(doc:Document, path:List[String]):Unit = {
    super.setup(doc, path)
    if (path isEmpty) 
      setupDocument(doc) 
    else setupTabs(doc, path)
  }

  def setupDocument(doc:Document) = {
    val docFrag = SectionBrowser(doc)
    Log.d(TAG, doc.toString)
    activity.doFragTrans {
      ft => {
        ft.add(android.R.id.content, docFrag)
        ft.attach(docFrag)
      }
    }
  }

  def setupTabs(doc:Document, path:List[String]) = {
    val section = (doc /~ path).current.asInstanceOf[Section]
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
    actionBar.setDisplayHomeAsUpEnabled(true)
    actionBar.removeAllTabs
    val titles = List("Sections", "Links", "Seqs") //todo replace these w/ resource
    def sb = SectionBrowser(section, path)
    def lb = LinkBrowser(section, path)
    def lsb = LinkSeqBrowser(section, path)
    val frags = activity.doFragTrans {
      ft => { 
        val content = android.R.id.content
        ft replace(content, sb) addToBackStack(
          (doc /~ path).pathStr)
        List(sb, lb, lsb)
      }
    }
    val tabs = (titles zip frags) map {
      pair => actionBar.newTab.setText(pair._1).setTabListener(
        new TabFragListener(pair._2))
    }
    tabs foreach (actionBar addTab _)
  }
}
