package org.cobalt.api.pathfinder.util

import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnel
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSet
import org.cobalt.api.pathfinder.pathing.configuration.PathfinderConfiguration
import org.cobalt.api.pathfinder.wrapper.PathPosition

class GridRegionData {
  private val bloomFilter: BloomFilter<PathPosition>
  private val regionalExaminedPositions: LongSet

  constructor(bloomFilterSize: Int, bloomFilterFpp: Double) {
    val pathPositionFunnel = Funnel<PathPosition> { pathPosition, into ->
      into.putInt(pathPosition.getFlooredX())
        .putInt(pathPosition.getFlooredY())
        .putInt(pathPosition.getFlooredZ())
    }

    bloomFilter = BloomFilter.create(pathPositionFunnel, bloomFilterSize, bloomFilterFpp)
    this.regionalExaminedPositions = LongOpenHashSet()
  }

  constructor(configuration: PathfinderConfiguration) : this(
    configuration.bloomFilterSize,
    configuration.bloomFilterFpp
  )

  fun getBloomFilter(): BloomFilter<PathPosition> = bloomFilter

  fun getRegionalExaminedPositions(): LongSet = regionalExaminedPositions
}
