//this subclass will create view that joins all three fragments into a single scrolling page, possibly using ScrollView
//another idea that didn't work out.
/*class VerticalSection(activity:LinkStorageActivity) extends LayoutMgr(activity) {
  val secID:Int = 1
  val linksID:Int = 2
  val lsID:Int = 3

  private def buildScroller(views:List[(View, LayoutParams)]):ScrollView = {
    val ll = new LinearLayout(activity)
    ll.setOrientation(LinearLayout.VERTICAL)
    views foreach {
      case (v, lllp) => ll.addView(v, lllp)
    }
    val sv = new ScrollView(activity)
    sv.addView(ll)
    sv
  }

  def createFragHolder(id:Int):List[(View, LayoutParams)] = {
    val space = new Space(activity)
    space.setMinimumHeight(10)
    val target = new FrameLayout(activity)
    target.setId(id)
    List((space, new LayoutParams(MATCH_PARENT, WRAP_CONTENT)),
      (target, new LayoutParams(MATCH_PARENT, MATCH_PARENT)))
  }

  def prepareView:View = {
    val ids =  List(secID, linksID, lsID)
    val list = ids.flatMap(createFragHolder(_))
    buildScroller(list)
  }

  def addDoc(docFrag:Fragment):Unit = {
    activity.doFragTrans {
      ft => ft.add(secID, docFrag)
    }
  }
  def addAll(secFrag:Fragment, linkFrag:Fragment, lsFrag:Fragment):Unit = {
    activity.doFragTrans {
      ft => {
        ft.replace(secID, secFrag)
        ft.replace(linksID, linkFrag)
        ft.replace(lsID, lsFrag)
        ft.addToBackStack(null)
      }
    }
  }
}*/

/* turns out this 1) doesn't work very wel, and 2) is ugly. screw it.
class TabbedSection(activity:LinkStorageActivity) extends LayoutMgr(activity) {
  import ActionBar.{Tab, TabListener}

  class TabFragListener(val frag:Fragment) extends TabListener {
    def onTabReselected(tab:Tab, ft:FragmentTransaction) = {/ do nothing /}
    def onTabSelected(tab:Tab, ft:FragmentTransaction) = ft.attach(frag)
    def onTabUnselected(tab:Tab, ft:FragmentTransaction) = ft.detach(frag)
  }

  def tearDown = {
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    actionBar.setDisplayHomeAsUpEnabled(false)
    actionBar.removeAllTabs
  }

  def prepare(frags:List[Fragment]):List[Int] = {
    actionBar.setHomeButtonEnabled(true)
    frags match {
      case List(docFrag) =>  prepareDoc(docFrag)
      case List(secFrag, linkFrag, linkSeqFrag) =>  prepareTabs(secFrag, linkFrag, linkSeqFrag)
    }
  }

  def prepareDoc(docFrag:Fragment) = {
    List(android.R.id.content)
  }

  def prepareTabs(secFrag:Fragment, linkFrag:Fragment, linkSeqFrag:Fragment) = {
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
    actionBar.setDisplayHomeAsUpEnabled(true)
    actionBar.removeAllTabs
    val titles = List("Sections", "Links", "Seqs") //todo replace these w/ resources
    val frags = List(secFrag, linkFrag, linkSeqFrag)
    val tabs = (titles zip frags) map {
      pair => actionBar.newTab.setText(pair._1).setTabListener(
        new TabFragListener(pair._2))
    }
    tabs foreach (actionBar addTab _)
    List(android.R.id.content, android.R.id.content, android.R.id.content)
  }
}
*/



