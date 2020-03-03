import spinal.core.sim._
import spinal.core._
import scala.util.Random
import org.scalatest.funsuite.AnyFunSuite

class RamTester extends AnyFunSuite {
  def genRandomCfg(bits: Int, words: Int, async: Boolean): RamCfg = {
    RamCfg (
      nWords      = words,
      nWrite      = Random.nextInt(5) + 1,
      nBits       = bits,
      nRead       = Random.nextInt(5) + 1,
      useRdEn     = Random.nextBoolean(),
      asyncReads  = async
    )
  }

  /**
   * Function which tests the RAM by reading and writing to
   * multiple ports and testing against randomly generated reference
   * @param ramCfg  the config of the ram
   * @param sim     sim instance containing compiled hdl
   */
  def runSim(ramCfg: RamCfg, sim: SimCompiled[MultiportRamComponent]): Unit = {
    // generate 32 16 bit random data items
    val randomData = (0 until ramCfg.nWords).map(_ => Random.nextInt() & 0xffff)
    val words = ramCfg.nWords

    sim.doSim(dut => {
      dut.clockDomain.forkStimulus(20)
      println("Async config sim run")
      println(s"nWrite: ${ramCfg.nWrite}, nRead: ${ramCfg.nRead}, async: ${ramCfg.asyncReads}")

      // put all read addresses to 0
      dut.io.raddr.foreach(_ #= 0)

      // write randomData into the ram nWrite values at a time
      var idx = 0
      var ctr = 0
      while (idx < words) {

        if (ramCfg.nWrite < (words - idx)) {
          // use all ports to write data
          (0 until ramCfg.nWrite).foreach(i => {
            dut.io.waddr(i) #= (i + idx)
            dut.io.wdata(i) #= randomData(i + idx)
            dut.io.wen(i) #= true
          })
          idx += ramCfg.nWrite
        } else {
          // use some ports to write data
          (0 until (words - idx)).foreach(i => {
            dut.io.waddr(i) #= (i + idx)
            dut.io.wdata(i) #= randomData(i + idx)
            dut.io.wen(i) #= true
          })

          // zero out other ports
          ((words - idx) until ramCfg.nWrite).foreach(i => {
            dut.io.waddr(i) #= 0
            dut.io.wen(i) #= false
          })

          idx = words
        }

        // wait for next cycle
        dut.clockDomain.waitSampling()
        ctr += 1
      }

      // all written, zero out the write ports
      dut.io.wen.foreach(_ #= false)
      dut.io.waddr.foreach(_ #= 0)
      dut.io.wdata.foreach(_ #= 0)

      def tick(): Unit = {
        // if we are testing a synchronous design then we need
        // to wait for clock edge for read data
        if (!ramCfg.asyncReads) {
          dut.clockDomain.waitSampling()
          ctr += 1
        }
        // this is needed, or else spinal will use previous value
        // not updated value
        sleep(1)
      }

      def driveReadAddr(addr: Int, port: Int): Unit = {
        dut.io.raddr(port) #= addr
        if (ramCfg.useRdEn) {
          dut.io.ren(port) #= true
        }
      }

      // now read back the data from the ports
      // and verify
      var checked = 0
      while (checked < words) {
        if (ramCfg.nRead <= (words - checked)) {
          // use all the read ports
          (0 until ramCfg.nRead).foreach (i => {
            driveReadAddr(checked + i, i)
          })

          tick()

          // verify
          (0 until ramCfg.nRead).foreach (i => {
            assert(dut.io.rdata(i).toInt == randomData(checked + i), s"[Cycle $ctr] Readport[$i]: Expected ${randomData(checked + i)}, Got ${dut.io.rdata(i).toInt}")
          })
          checked += ramCfg.nRead
        }
        else {
          val remaining = words - checked

          // use some ports to read remaining
          (0 until remaining).foreach(i => {
            driveReadAddr(checked + i, i)
          })

          // no need to zero out other read ports
          // they are harmless

          tick()

          // verify
          (0 until remaining).foreach (i => {
            assert(dut.io.rdata(i).toInt == randomData(checked + i), s"[Cycle $ctr] Readport[$i]: Expected ${randomData(checked + i)}, Got ${dut.io.rdata(i).toInt}")
          })
          checked = words
        }
      }
    })
  }

  val w: Int = 64   // number of words in ram
  val n: Int = 5    // number of sim runs

  Random.setSeed(33)

  // create 'n' random configurations
  val ramCfgs = Array.fill(n)(genRandomCfg(16, w, async = Random.nextBoolean()))
  val spinalConfig = SpinalConfig()
  var sims: Array[SimCompiled[MultiportRamComponent]] = _
  // compile those configs
  test("Compile") {
    sims = ramCfgs.map(c => SimConfig
      .withWave
      .workspacePath("waves")
      .withConfig(spinalConfig)
      .compile(MultiportRamComponent(c))
    )
  }

  // run the sims
  test("Simulate and Verify") {
    ramCfgs.zip(sims).foreach(x => runSim(x._1, x._2))
  }
}
