package local.nodens.linkstorage.data
import local.nodens.linkstorage._
import local.nodens.linkmodel._
import scala.collection.JavaConversions._
import android.view._
import android.widget._
import android.content.Context

import android.util.Log
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
  import com.commonsware.cwac.sacklist.SackOfViewsAdapter
  //gets the index of the adapter containing the passed in pos
  //in the list of adapters.
  import scala.collection.mutable.ArrayBuffer
  import scala.collection.mutable.Map
  val lowers = ArrayBuffer.empty[Int]
  val uppers = ArrayBuffer.empty[Int] // the first upper is the size of the first element
  val pureAdapterToPieceIndex = ArrayBuffer.empty[Int]
  val pieceIndexToPureAdapter = Map.empty[Int, Int]

  override def addAdapter(adapter:ListAdapter):Unit = {
    super.addAdapter(adapter)
    val count = adapter.getCount
    val lastUpper = uppers.lastOption getOrElse 0
    lowers.append(lastUpper)
    uppers.append(count + lastUpper)
    if (adapter.isInstanceOf[SackOfViewsAdapter]) { }
    else {
      pureAdapterToPieceIndex.append(pieces.size - 1)
      pieceIndexToPureAdapter.put(pieces.size -1, pureAdapterToPieceIndex.size - 1)
    }
  }

  @EnhanceStrings
  def getAdapterPos(pos:Int):Int = {
    val bounds = lowers zip uppers
    val containing = bounds.zipWithIndex.filter {
      case (b, idx) => (pos >= b._1) && pos < b._2
    }
    val pieceIndex = containing.toList.head._2
    val aPos = (pieceIndexToPureAdapter get pieceIndex).get
    aPos
  }

  @EnhanceStrings
  def getSubAdapterPosition(pureAdapter:Int, pos:Int) = {
    val pieceIndex = pureAdapterToPieceIndex(pureAdapter)
    val fPos = pos - lowers(pieceIndex) 
    fPos
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
