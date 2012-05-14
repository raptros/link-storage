package local.nodens.linkstorage.data
import local.nodens.linkstorage._
import local.nodens.linkmodel._
import scala.collection.JavaConversions._
import android.view._
import android.widget._
import android.content.Context

import scalaz._
import Scalaz._
import com.commonsware.cwac.merge.MergeAdapter

/**
 * Adapter for displaying section lists.
 */
class AdaptSection(val context:Context, sections:Iterable[String]) extends BaseAdapter {
  val keyArray = sections.toArray

  def getView(pos:Int, convert:View, parent:ViewGroup):View = convert match {
    case si:SectionItem => si.update(keyArray(pos))
    case _ => SectionItem(context, keyArray(pos))
  }
  def getItemId(pos:Int):Long = pos.longValue()
  def getItem(pos:Int):Object = keyArray(pos)
  def getCount:Int = keyArray.length
}

class AdaptLink(val context:Context, links:Seq[Link]) extends BaseAdapter {
  val linkArr = links.toArray
  def update(li:LinkItem, link:Link) = li.update(link.title, link.url)
  def newLI(link:Link) = LinkItem(context, link.title, link.url)
  def getView(pos:Int, convert:View, parent:ViewGroup):View = convert match {
    case (li:LinkItem) => update(li, linkArr(pos))
    case _ => newLI(linkArr(pos))
  }
  def getItemId(pos:Int):Long = pos.longValue()
  def getItem(pos:Int):Object = linkArr(pos)
  def getCount:Int = linkArr.length
}


abstract class MergeAdapterWithIndices extends MergeAdapter {
  def getAdapterPos(pos:Int):Int = {
    val offsets = pieces.map(_.getCount).scanLeft(0)(_+_).sliding(2)
    offsets.map {
      case Seq(l, u) => (l, u)
    }.zipWithIndex.filter {
      case (b, idx) => (pos >= b._1) && pos < b._2
    }.toList.head._2
  }
}

/**
 * Adapter for displaying link sequences.
 */
class AdaptLinkSeq(val context:Context, linkSeqs:Seq[LinkSeq]) extends MergeAdapterWithIndices {
  val seqArray = linkSeqs.toArray
  val adapterArray = seqArray map (ls => new AdaptLink(context, ls.links))

  adapterArray.indices foreach {
    idx => {
      addView(SeparatorItem(context, idx))
      addAdapter(adapterArray(idx))
    }
  }
}
