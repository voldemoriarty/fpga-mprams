/**
 * Configuration for rams
 * @param nWords      number of words (depth)
 * @param nWrite      number of write ports
 * @param nBits       bits per word (width)
 * @param nRead       number of read ports
 * @param useRdEn     use a read enable signal in each read port
 * @param asyncReads  use async read style
 * @param mlabAttr    Add an attribute to force Quartus to infer MLABS. Useful with asyncReads
 */
case class RamCfg(
                 nWords: Int = 32,
                 nWrite: Int = 2,
                 nBits: Int = 32,
                 nRead: Int = 4,
                 useRdEn: Boolean = true,
                 asyncReads: Boolean = false,
                 mlabAttr: Boolean = false
                 )
