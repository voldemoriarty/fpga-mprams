import spinal.core._

object MultiportRamGenerator {
  def main(args: Array[String]): Unit = {
    val cfg = RamCfg(
      nWrite = 3,
      nRead = 2,
      mlabAttr = true
    )
    SpinalConfig().generateVerilog(MultiportRamComponent(cfg)).printPruned()
  }
}
