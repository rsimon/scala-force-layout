package at.ait.dme.forcelayout.renderer

import java.awt.Color

/**
 * Color palette re-uses colors from D3 - http://d3js.org/
 */
object ColorPalette {
  
  private val palette = Seq(
      new Color(31, 119, 180),
      new Color(255, 127, 14),
      new Color(44, 160, 44),
      new Color(214, 39, 40),
      new Color(148, 103, 189),
      new Color(140, 86, 75),
      new Color(227, 119, 194),
      new Color(127, 127, 127),
      new Color(188, 189, 34),
      new Color(23, 190, 207))
      
  def getColor(idx: Int) = palette(idx % palette.size)

}