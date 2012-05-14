package local.nodens.linkstorage.layout
import local.nodens.linkstorage._
import local.nodens.linkstorage.data._

import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.util.Log
import local.nodens.linkmodel._
import android.content._
import ActionBar.{Tab, TabListener}
import AdapterView.OnItemClickListener
import scalaz._
import Scalaz._

class TabLayout2(activity:LinkStorageActivity) extends LayoutMgr(activity) {
  val content = android.R.id.content

  val view = new ListView(activity)

  def sectionCallback(adapter:AdaptSection) = new OnItemClickListener {
    def onItemClick(parent:AdapterView[_], view:View, position:Int, id:Long) = activity.onSectionSelect(adapter.keyArray(position))
  }

  def linkCallback(adapter:AdaptLink) = new OnItemClickListener {
    def onItemClick(parent:AdapterView[_], view:View, position:Int, id:Long) = activity.onLinkSelect(position)
  }

  def linkSeqCallback(adapter:AdaptLinkSeq) = new OnItemClickListener {
    def onItemClick(parent:AdapterView[_], view:View, position:Int, id:Long) = activity.onLinkSeqItemSelect(
      adapter.getAdapterPos(position), position)
  }


  class MTabListener(val adapter:ListAdapter, val callback:OnItemClickListener) extends TabListener {
    def onTabReselected(tab:Tab, ft:FragmentTransaction) = {/* do nothing */}
    def onTabSelected(tab:Tab, ft:FragmentTransaction) = {
      view.setOnItemClickListener(callback)
      view.setAdapter(adapter)
    }
    def onTabUnselected(tab:Tab, ft:FragmentTransaction) = { }
  }

  def prepareView:View = {
    view
  }

  def displayDoc(doc:Document) = {
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    val adapter = new AdaptSection(activity, doc.sections.keys)
    view.setOnItemClickListener(sectionCallback(adapter))
    view.setAdapter(adapter)
  }
  
  def displaySection(sec:SecStackItem) = {
    val section = (sec.doc /~ sec.path).current.asInstanceOf[Section]
    actionBar.removeAllTabs()
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS | ActionBar.NAVIGATION_MODE_STANDARD)
    val builds = (secTab(_,_))::(linkTab(_,_))::(linkSeqTab(_,_))::Nil 
    val tabs = builds flatMap (f => f(section, sec)) 
    tabs foreach (actionBar.addTab(_))
    tabs.headOption foreach (actionBar.selectTab(_))
  }

  def secTab(sec:Section, ssi:SecStackItem) = ssi.hasSections.option {
    val adapter = new AdaptSection(activity, sec.sections.keys)
    buildATab("Section", adapter, sectionCallback(adapter))
  }
  
  def linkTab(sec:Section, ssi:SecStackItem) = ssi.hasLinks.option {
    val adapter = new AdaptLink(activity, sec.links)
    buildATab("Links", adapter, linkCallback(adapter))
  }

  def linkSeqTab(sec:Section, ssi:SecStackItem) = ssi.hasLinkSeqs.option {
    val adapter = new AdaptLinkSeq(activity, sec.linkSeqs)
    buildATab("Link Seqs", adapter, linkSeqCallback(adapter))
  }

  def buildATab(title:String, adapter:ListAdapter, callback:OnItemClickListener):Tab = actionBar.newTab().setText(title).setTabListener(new MTabListener(adapter, callback))
}
