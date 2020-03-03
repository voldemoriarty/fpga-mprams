import spinal.core._

case class Lvt (cfg: RamCfg = RamCfg())  {
  val dataWidth = cfg.nBits bits
  val depth     = cfg.nWords
  val addrWidth = log2Up(cfg.nWords) bits

  val lvtWidth  = log2Up(cfg.nWrite) bits

  val lvt = Vec(Reg(UInt(lvtWidth)), cfg.nWords)
  var writePortCounter = 0

  def read(addr: UInt): UInt = {
    assert(addr.getBitsWidth <= log2Up(cfg.nWords), "Address out of range")
    lvt(addr.resize(addrWidth))
  }

  def write(addr: UInt, enable: Bool): Unit = {
    assert(writePortCounter <= cfg.nWrite, "Write ports greater than in cfg")
    when (enable) {
      lvt(addr) := IntToUInt(writePortCounter).resize(lvtWidth)
    }
    writePortCounter += 1
  }
}