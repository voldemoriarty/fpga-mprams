import spinal.core._

object LvtGenerator {
  def main(args: Array[String]): Unit = {
    val cfg = RamCfg(nWords = 256, nWrite = 4, nBits = 64, nRead = 6)
    SpinalConfig(device = Device.ALTERA).generateVerilog(LvtComponent(cfg))
  }
}
