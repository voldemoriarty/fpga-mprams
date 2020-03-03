# FPGA Multiport RAMs
An LVT based implementation based on 
[this](http://www.eecg.toronto.edu/~steffan/papers/laforest_fpga10.pdf)
paper by Laforest and Steffan using SpinalHDL

# Features
 - Use Asynchronous or Synchronous RAMs
 - Can infer MLAB blocks in new Altera Devices (using attributes) for async
 or sync rams
 - Generic: Any number of read and write ports (Must be greater than zero)
 
 
# Dependencies
 - spinalHDL
 - scaltest (for verification)
 
# Generation
A basic example is given in `src/main/scala/MultiportRamGenerator.scala`

All the properties are defined in the `RamCfg` class. The following are the
parameters that are used
 - `nWords: Int` Number of words in RAM
 - `nWrite: Int` Number of write ports
 - `nBits: Int` Number of bits per word
 - `nRead: Int` Number of read ports
 - `useRdEn: Boolean` Use read enable signals
 - `asyncReads: Boolean` Do reads asynchronously
 - `mlabAttr: Boolean` Force Infer MLABs (Altera Quartus only)

# Verification Strategy
Multiple random configurations (16 wide, 32 deep) are created covering both synchronous and 
asynchronous modes with random number of read and write ports (constrained
within 1 to 5). They are first written with random 16 bit data using all
write ports and then it is read back using all read ports. It is then checked if
the readback data matched the data that was written
