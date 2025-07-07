package sangria.streaming

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{ActorMaterializer, Materializer}
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class PekkoStreamsIntegrationSpec extends AnyWordSpec with Matchers {
  implicit val system: ActorSystem = ActorSystem("test")
  implicit val mat: Materializer = Materializer.matFromSystem

  private val awaitDuration: FiniteDuration = 2.seconds

  val impl: SubscriptionStream[pekkoStreams.PekkoSource] =
    new pekkoStreams.PekkoStreamsSubscriptionStream

  "AkkaStreams Integration" should {
    "support itself" in {
      impl.supported(pekkoStreams.pekkoSubscriptionStream) should be(true)
    }

    "map" in {
      res(impl.map(source(1, 2, 10))(_ + 1)) should be(List(2, 3, 11))
    }

    "singleFuture" in {
      res(impl.singleFuture(Future.successful("foo"))) should be(List("foo"))
    }

    "single" in {
      res(impl.single("foo")) should be(List("foo"))
    }

    "mapFuture" in {
      res(impl.mapFuture(source(1, 2, 10))(x => Future.successful(x + 1))) should be(List(2, 3, 11))
    }

    "first" in {
      res(impl.first(source(1, 2, 3))) should be(1)
    }

    "first throws error on empty" in {
      an[NoSuchElementException] should be thrownBy res(impl.first(source()))
    }

    "failed" in {
      an[IllegalStateException] should be thrownBy res(
        impl.failed(new IllegalStateException("foo")))
    }

    "onComplete handles success" in {
      val count = new AtomicInteger(0)
      def inc() = count.getAndIncrement()

      val updated = impl.onComplete(source(1, 2, 3))(inc())

      Await.ready(updated.runWith(Sink.last), 2.seconds)

      count.get() should be(1)
    }

    "onComplete handles failure" in {
      val s = source(1, 2, 3).map { i =>
        if (i == 2) throw new IllegalStateException("foo")
        else i
      }

      val count = new AtomicInteger(0)
      def inc() = count.getAndIncrement()

      val updated = impl.onComplete(s)(inc())

      Await.ready(updated.runWith(Sink.last), 2.seconds)

      count.get() should be(1)
    }

    "flatMapFuture" in {
      res(impl.flatMapFuture(Future.successful(1))(i =>
        source(i.toString, (i + 1).toString))) should be(List("1", "2"))
    }

    "recover" in {
      val obs = source(1, 2, 3, 4).map { i =>
        if (i == 3) throw new IllegalStateException("foo")
        else i
      }

      res(impl.recover(obs)(_ => 100)) should be(List(1, 2, 100))
    }

    "merge" in {
      val obs1 = source(1, 2)
      val obs2 = source(3, 4)
      val obs3 = source(100, 200)

      val result = res(impl.merge(Vector(obs1, obs2, obs3)))

      result should have(size(6))
        .and(contain(1))
        .and(contain(2))
        .and(contain(3))
        .and(contain(4))
        .and(contain(100))
        .and(contain(200))
    }

    "merge 2" in {
      val obs1 = source(1, 2)
      val obs2 = source(100, 200)

      val result = res(impl.merge(Vector(obs1, obs2)))

      result should have(size(4))
        .and(contain(1))
        .and(contain(2))
        .and(contain(100))
        .and(contain(200))
    }

    "merge 1" in {
      val obs1 = source(1, 2)

      val result = res(impl.merge(Vector(obs1)))

      result should have(size(2)).and(contain(1)).and(contain(2))
    }

    "merge throws exception on empty" in {
      an[IllegalStateException] should be thrownBy impl.merge(Vector.empty)
    }
  }

  def source[T](elems: T*): Source[T, NotUsed] =
    Source.fromIterator(() => Iterator(elems: _*))

  def res[T](s: Source[T, NotUsed]): Seq[T] =
    Await.result(s.runFold(List.empty[T]) { case (acc, e) => acc :+ e }, awaitDuration)

  def res[T](f: Future[T]): T =
    Await.result(f, awaitDuration)
}
