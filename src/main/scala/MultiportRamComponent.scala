import spinal.core._

/**
 * Multiport Ram top
 * @param cfg:      ram configuration (number of read ports etc)
 * @param ioPrefix: have the "io" attached to ports in generated verilog/vhdl
 */
case class MultiportRamComponent(cfg: RamCfg = RamCfg(), ioPrefix: Boolean = false) extends Component {
  assert(cfg.nWrite > 0, "You must have at least one write port")
  assert(cfg.nRead > 0, "You must have at least one read port")

  if (!ioPrefix) noIoPrefix()

  def address = UInt(log2Up(cfg.nWords) bits)
  def data    = UInt(cfg.nBits bits)

  val io = new Bundle {
    val raddr = in  Vec(address, cfg.nRead)
    val ren   = if (cfg.useRdEn) in Vec(Bool, cfg.nRead) else null
    val rdata = out Vec(data, cfg.nRead)
    val waddr = in  Vec(address, cfg.nWrite)
    val wen   = in  Vec(Bool, cfg.nWrite)
    val wdata = in  Vec(data, cfg.nWrite)
  }

  // create an LVT
  val lvt   = Lvt(cfg)
  // create nWrite rams of 1WnR fashion
  val rams  = Array.fill(cfg.nWrite)(new Mem(UInt(cfg.nBits bits), cfg.nWords))
  // if add the mlab attribute then
  // quartus won't infer mlabs unless "no_rw_check" attribute is given
  if (cfg.mlabAttr) rams.foreach(_.addAttribute("ramstyle", "no_rw_check, mlab"))

  // each mem is N-read-port 1-write-port ram
  val reads = Vec(rams.map(mem => {
    val read: Vec[UInt] = Vec(data, cfg.nRead)
    for ((r, i) <- read.zipWithIndex) {
      if (cfg.asyncReads) {
        r := mem.readAsync(io.raddr(i))
      } else {
        r := mem.readSync(io.raddr(i), if (cfg.useRdEn) io.ren(i) else null)
      }
    }
    read
  }))

  // access the LVT to find which bank has correct
  // value. Also register the output if the rams
  // are synchronous
  val lvr   = Vec(io.raddr.map(lvt.read))
  val banks = if (cfg.asyncReads) lvr else RegNext(lvr)

  // for each port, use the bank value from LVT to
  // check which memory block has right value
  for ((rdataport, i) <- io.rdata.zipWithIndex) {
    rdataport := reads(banks(i))(i)
  }

  // assign each memory block a write port
  // also update LVT
  for ((ram, i) <- rams.zipWithIndex) {
    ram.write(io.waddr(i), io.wdata(i), io.wen(i))
    lvt.write(io.waddr(i), io.wen(i))
  }
}