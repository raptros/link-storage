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
  //listener in the tab bar.
  class MTabListener(val view:ListView, val adapter:ListAdapter, val callback:OnItemClickListener) extends TabListener {
    def onTabReselected(tab:Tab, ft:FragmentTransaction) = {/* do nothing */}
    def onTabSelected(tab:Tab, ft:FragmentTransaction) = {
      view.setOnItemClickListener(callback)
      view.setAdapter(adapter)
    }
    def onTabUnselected(tab:Tab, ft:FragmentTransaction) = { }
  }

  def adapterListener[A <: ListAdapter](adapter:A)(callback: (A, Int) => Unit) = new OnItemClickListener {
    def onItemClick(parent:AdapterView[_], view:View, position:Int, id:Long) = callback(adapter, position)
  }

  //build tabs easily
  class TabConstructor(val view:ListView, val sec:Section, val ssi:SecStackItem) {
    def buildATab[A <: ListAdapter](title:String, adapter:A)(callback: (A, Int) => Unit):Tab = {
      val onItem = adapterListener(adapter)(callback)
      val listener = new MTabListener(view, adapter, onItem)
      actionBar.newTab().setText(title).setTabListener(listener)
    }

    def optionBuildTab[A <: ListAdapter](condition:Boolean)(title:String, adapter:A)(callback: (A, Int) => Unit) = condition.option(buildATab(title, adapter)(callback))
    
    def secTab:Option[Tab] = optionBuildTab(ssi.hasSections)("Section", new AdaptSection(activity, sec.sections.keys)) {
      (adapter, pos) => activity.onSectionSelect(adapter.keyArray(pos))
    }
    def linkTab:Option[Tab] = optionBuildTab(ssi.hasLinks)("Links", new AdaptLink(activity, sec.links)) {
      (adapter, pos) => activity.onLinkSelect(pos)
    }
    @EnhanceStrings
    def linkSeqTab:Option[Tab] = optionBuildTab(ssi.hasLinkSeqs)("Link Seqs", new AdaptLinkSeq(activity, sec.linkSeqs)) {
      (adapter, pos) => {
        val seq = adapter.getAdapterPos(pos)
        val idx = adapter.getSubAdapterPosition(seq, pos)
        activity.onLinkSeqItemSelect(idx, seq)
      }
    }
  }

  val kSelectedTab = "tab_layout2_selected_tab"

  def prepareView:View = {
    LayoutInflater.from(activity).inflate(R.layout.tab_layout2_list, null)
  }

  def displayDoc(doc:Document) = {
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    val view = activity.findViewById(R.id.tab_layout2_list_view).asInstanceOf[ListView]
    val adapter = new AdaptSection(activity, doc.sections.keys)
    val callback = adapterListener[AdaptSection](adapter) {
      (adapter, pos) => activity.onSectionSelect(adapter.keyArray(pos))
    }
    view.setOnItemClickListener(callback)
    view.setAdapter(adapter)
  }
  
  def displaySection(sec:SecStackItem) = {
    val section = (sec.doc /~ sec.path).current.asInstanceOf[Section]
    actionBar.removeAllTabs()
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS | ActionBar.NAVIGATION_MODE_STANDARD)
    val view = activity.findViewById(R.id.tab_layout2_list_view).asInstanceOf[ListView]
    val tabCons = new TabConstructor(view, section, sec) //somehow get view
    val tabs:List[Tab] = (tabCons.secTab::tabCons.linkTab::tabCons.linkSeqTab::Nil).flatten
    tabs foreach (actionBar.addTab(_))
    tabs.headOption foreach (actionBar.selectTab(_))
  }

  def restoreInstanceState(sis:Option[Bundle]):Unit =  for {
    bundle <- sis
    tabTag <-Option(bundle getString kSelectedTab)
    index <- (0 until actionBar.getTabCount)
    tabItem = actionBar.getTabAt(index)
    tab <- (tabItem.getText.toString == tabTag).option(tabItem)
  } (actionBar.selectTab(tab))

  def onSaveInstanceState(outSIS:Bundle):Unit = Option(actionBar.getSelectedTab) foreach{
    tab => outSIS.putString(kSelectedTab, tab.getText.toString)
  }
}
