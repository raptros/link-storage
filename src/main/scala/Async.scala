package local.nodens.linkstorage
import local.nodens.linkmodel._
import android.os.{AsyncTask, Environment}
import java.lang.Void
import java.io.{File, InputStream}
import android.util.Log
import android.content.res.Resources
import scala.xml.{XML, Node}
import scala.runtime.RichInt


import scalaz._
import Scalaz._

//everything needed to extract a single anyref argument as a proper type.
trait GetsArg {
  def getArg[A](args:Array[AnyRef])(implicit m:Manifest[A]):Validation[LoadFail, A] = extractArg(args) flatMap (convertArg[A](_)(m))
  def extractArg(args:Array[AnyRef]):Validation[LoadFail, AnyRef] = validation(args.isEmpty either "could not get argument" or args(0))
  def convertArg[A](arg:AnyRef)(implicit m:Manifest[A]):Validation[LoadFail, A] = {
    validation(!arg.isInstanceOf[A] either "could not extract arg as " + m.toString or arg.asInstanceOf[A])
  }
}

trait ParsesFile { def parseFile(node:Node):Validation[LoadFail, Document] = Document.parse(node).fold(_.success, "could not parse the xml".fail) }

trait DoesCallbacks {
  def loaded:OnLoad
  def failed:OnLoadFail
  def doCallback(res:Loaded) = res.fold(failed(_), loaded(_))
}

trait GetsDir {
  def getDir:Validation[LoadFail, File] = Option(Environment.getExternalStorageDirectory()).fold(_.success, "could not get base directory".fail)
}

//abstract loader puts the loaded document into the activity inside display thread
abstract class AbstractLoader(val loaded:OnLoad, val failed:OnLoadFail) extends AsyncTask[AnyRef, Void, Loaded]
with GetsArg with ParsesFile with DoesCallbacks { 
  override protected def onPostExecute(r:Loaded) = doCallback(r)
}

//load doc from the sdcard
class DocLoader(loaded:OnLoad, failed:OnLoadFail) extends AbstractLoader(loaded, failed) with GetsDir {
  def loadFile(file:File):Validation[LoadFail, Node] = try {
    XML.loadFile(file).success
  } catch {
    case t:Throwable => ("file at " + file.getCanonicalPath + " could not be loaded because " + t.getMessage).fail
  }
  override protected def doInBackground(args:AnyRef*) = for { //it's monadic!
    path <- getArg[String](args.toArray)
    dir <- getDir
    file = new File(dir, path)
    loaded <- loadFile(file)
    doc <- parseFile(loaded)
  } yield doc
}

//load doc from raw resources.
class RawDocLoader(val res:Resources, loaded:OnLoad, failed:OnLoadFail) extends AbstractLoader(loaded, failed) {
  def loadStream(id:Int):Validation[LoadFail, InputStream] = try {
    res.openRawResource(id).success
  } catch {
    case t:Throwable => ("could not load resource " + id + " because " + t.getMessage).fail
  }
  def loadXML(is:InputStream):Validation[LoadFail, Node] = try {
    XML.load(is).success
  } catch {
    case t:Throwable => ("could not load xml because " + t.getMessage).fail
  }
  override protected def doInBackground(args:AnyRef*):Loaded = for {
    id <- getArg[RichInt](args.toArray)
    stream <- loadStream(id.toInt)
    xml <- loadXML(stream)
    doc <- parseFile(xml)
  } yield doc
}
