package local.nodens.linkstorage.layout
import local.nodens.linkstorage._
import browser.{SectionBrowser, LinkBrowser, LinkSeqBrowser}

import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.util.Log
import local.nodens.linkmodel._
import android.content._
import ActionBar.{Tab, TabListener}

import scalaz._
import Scalaz._

class TabLayout2(activity:LinkStorageActivity) extends LayoutMgr(activity) {
  val content = android.R.id.content

  class TabFragListener(val frag:Fragment) extends TabListener {
    def onTabReselected(tab:Tab, ft:FragmentTransaction) = {/* do nothing */}
    def onTabSelected(tab:Tab, ft:FragmentTransaction) = ensureFrag(ft, frag, content)
    def onTabUnselected(tab:Tab, ft:FragmentTransaction) = ft.remove(frag)
  }

  def prepareView:View = {
    new FrameLayout(activity) //candidate for a default view
  }

  def addDoc(docFrag:Fragment):Unit = {
    activity.doFragTrans (_.add(content, docFrag))
  }

  def addAll(secFrag:Fragment, linkFrag:Fragment, lsFrag:Fragment):Unit = {
    actionBar.removeAllTabs()
    activity.doFragTrans {
      ft => {
        ensureFrag(ft, secFrag, content)
        ft.addToBackStack(null)
      }
    }
  }

  def buildATab(title:String, oFrag:Option[Fragment]):Option[Tab] = oFrag map {
    frag => actionBar.newTab().setText(title).setTabListener(new TabFragListener(frag))
  }

  def examineCurrent():Unit = {
    if (current.path.isEmpty) { //i.e. at doc root.
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    } else {
      val build = (buildATab(_, _)).tupled
      val pairs = current.hasSections.option("Section" -> current.oSec) ::
      current.hasLinks.option("Links" -> current.oLinks) ::
      current.hasLinkSeqs.option("Link Seqs" -> current.oLS) :: Nil
      val tabs = pairs.flatten flatMap (build(_))
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS | ActionBar.NAVIGATION_MODE_STANDARD)
      tabs foreach (actionBar.addTab(_))
    }
  }

  override def popSection():Unit = {
    actionBar.removeAllTabs()
    //activity.doFragTrans(_.remove(fragMan.findFragmentById(content)))
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    super.popSection()
  }
}
