import spinal.core._

case class LvtComponent(cfg: RamCfg = RamCfg(), ioPrefix : Boolean = false) extends Component {
  val dataWidth = cfg.nBits bits
  val depth     = cfg.nWords
  val addrWidth = log2Up(cfg.nWords) bits

  val lvtWidth  = log2Up(cfg.nWrite) bits

  val io = new Bundle {
    val ra = in   Vec(UInt(addrWidth), cfg.nRead)
    val wa = in   Vec(UInt(addrWidth), cfg.nWrite)
    val we = in   Vec(Bool, cfg.nWrite)
    val rd = out  Vec(UInt(lvtWidth), cfg.nRead)
  }

  val lvt = Lvt(cfg)

  io.rd := Vec(io.ra.map(lvt.read))
  for (wport <- io.wa.zip(io.we)) {
    lvt.write(wport._1, wport._2)
  }

  // no io prefix in generated code
  if (!ioPrefix) {
    noIoPrefix()
  }
}
