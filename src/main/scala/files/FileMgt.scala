package local.nodens.linkstorage.files
import local.nodens.linkstorage._
import local.nodens.linkmodel._

import android.app._
import android.os.{Bundle, Environment}
import android.util.Log
import android.content.res.Resources

import java.io.{File, InputStream,Writer,BufferedWriter,FileWriter}

import scala.concurrent.ops._
import scala.xml.{XML, Node}
import scala.collection.immutable.Stream
import scala.collection.mutable.StringBuilder
import scala.collection.JavaConversions
import scala.util.Random

import scalaz._
import Scalaz._

trait ProtectOps {
  //heh.
  def protect[A](msg: Throwable => FileFailure)(f: => A):Validation[FileFailure, A] = try{
    f.success
  } catch {
    case (t:Throwable) => msg(t).fail
  }
}

/**
 * The ability to load and save files.
 */
@EnhanceStrings
trait AsyncOps extends ProtectOps with HasLSA {
  implicit def toRunnable[F](f: => F): Runnable = new Runnable() { def run() = f }

  def msg[A](m:String)(v:Validation[Throwable, A]):Validation[FileFailure, A] = v fold (t => (m + t.getMessage).fail, a => a.success)

  def parseFile(node:Node):Validation[LoadFail, Document] = Document.parse(node).toSuccess("could not parse the xml")
  //generally shouldn't fail on android 4.
  def getDir:Validation[FileFailure, File] = Option(activity.getFilesDir).toSuccess("could not get base directory")
  
  def loadRaw(res:Int)(onFail: FileFailure => Unit)(onLoad: Document => Unit) = spawn {
    val loaded = for {
      stream <- {() => activity.getResources.openRawResource(res)}.throws |> msg ("could not open #res because ")
      xml <- {() => XML.load(stream)}.throws |> msg("could not load xml because ")
      doc <- parseFile(xml)
    } yield doc

    //next works also b/c of implicit def
    activity.runOnUiThread { loaded.fold(onFail(_), onLoad(_)) }
  }

  def load(path:String)(onFail: FileFailure => Unit)(onLoad: Document => Unit) = spawn {
    val loaded = for {
      dir <- getDir
      file = new File(dir, path)
      xml <- {() => XML.loadFile(file)}.throws |> msg("could not load #file.getCanonicalPath because ")
      doc <- parseFile(xml)
    } yield doc
    
    activity.runOnUiThread { loaded.fold(onFail(_), onLoad(_)) }
  }

  def save(path:String, doc:Document)(onFail:FileFailure => Unit) = spawn {
    Log.d(TAG, "running save")
    val saveRes = for {
      dir <- getDir
      file = new File(dir, path)
      writer <- {() => new BufferedWriter(new FileWriter(file))}.throws |> msg("could not open file because ")
      str = doc.renderXML.toString
      written <- {() => writer.write(str)}.throws |> msg("could not write file because ")
      closed <- {() => writer.close}.throws |> msg("error occured in writing ")
    } yield {Log.d(TAG, "made it to end of save loop"); closed}
    
    activity.runOnUiThread {saveRes.fold(onFail(_), _ => { } )}
  }
}

//A trait for managing the directory
trait DirectoryMgr extends HasLSA {
  def files:List[String] = Option(activity.getFilesDir).map (_.list.toList).flatten.toList
  //def files:List[String] = Option(activity.fileList) some(_ toList) none(Nil) 

  private def randName(len:Int) = {
    val chars = (0 until len) >| Random.nextPrintableChar() 
    val name = chars.foldLeft(new StringBuilder)(_+=_) ++= ".xml" 
    name.toString
  }

  def getUniqueName():String = {
    val fileSet = files.toSet
    val names = Stream.continually(randName(8))//lots of file names this way
    names.find(!fileSet.contains(_)) get //might never terminate, but will terminate with some.
  }
}

/**
 * ... Store the internal mutable state about what file is loaded, where to save, etc.
 * Sons of guns, I feel dirty.
 */
class FileMgr(val activity:LinkStorageActivity) extends AsyncOps with DirectoryMgr {

  private var currentRaw:Option[Int] = None
  private var currentTarget:Option[String] = None

  def onFail(ff:FileFailure) = Log.d(TAG, ff)

  def doLoadRaw(id:Int)(post: Document => Unit):Unit =  loadRaw(id)(onFail _){currentRaw = id.some; post}
  def doLoadFile(fname:String)(post: Document => Unit):Unit = load(fname)(onFail _){currentTarget = fname.some; post}

  //def canSave = currentTarget.isSome && currentRaw.isNone

  def doSaveFile(doc:Document)(fname:String) = save(fname, doc)(onFail)

  def saveNow(doc:Document):Unit = currentTarget |>| doSaveFile(doc) //don't save unless there's already a target

  //sis must not be null
  def restoreInstanceDoc(sis:Bundle)(post: Document => Unit ) = {
    val raw = (sis containsKey kSISDocID) option(sis getInt kSISDocID) map (doLoadRaw _)
    val file = Option(sis getString kSISDocFile) map (doLoadFile _)
    (raw orElse file) |>| (_ apply post)
  }

  def onSaveInstanceState(outSIS:Bundle) = currentRaw some(outSIS.putInt(kSISDocID, _)) none(currentTarget |>| (outSIS.putString(kSISDocFile, _)))

}
