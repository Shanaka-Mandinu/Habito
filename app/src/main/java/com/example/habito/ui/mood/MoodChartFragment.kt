package com.example.habito.ui.mood

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.habito.databinding.FragmentMoodChartBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class MoodChartFragment : Fragment() {
    private var _binding: FragmentMoodChartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoodChartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupChart()
        observeViewModel()
        
        viewModel.loadMoodData()
    }
    
    private fun setupChart() {
        binding.lineChart.apply {
            description.text = "7-Day Mood Trend"
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            // Configure X axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                isGranularityEnabled = true
            }
            
            // Configure Y axis
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 5f
                setDrawGridLines(true)
                granularity = 1f
                isGranularityEnabled = true
            }
            
            axisRight.isEnabled = false
            
            // Configure legend
            legend.isEnabled = true
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chartData.collect { data ->
                updateChart(data)
                binding.progressBarLoading.visibility = View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun updateChart(data: List<Pair<String, Float>>) {
        if (data.isEmpty()) {
            binding.textViewNoData.visibility = View.VISIBLE
            binding.lineChart.visibility = View.GONE
            return
        }
        
        binding.textViewNoData.visibility = View.GONE
        binding.lineChart.visibility = View.VISIBLE
        
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        data.forEachIndexed { index, (date, mood) ->
            entries.add(Entry(index.toFloat(), mood))
            labels.add(date)
        }
        
        val dataSet = LineDataSet(entries, "Mood Level").apply {
            color = Color.parseColor("#2196F3")
            setCircleColor(Color.parseColor("#2196F3"))
            lineWidth = 3f
            circleRadius = 6f
            setDrawCircleHole(false)
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = Color.parseColor("#E3F2FD")
        }
        
        val lineData = LineData(dataSet)
        
        binding.lineChart.apply {
            this.data = lineData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            invalidate() // Refresh the chart
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}