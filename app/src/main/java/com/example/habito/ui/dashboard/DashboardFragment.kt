package com.example.habito.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.habito.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import com.example.habito.database.repositories.UserRepository
import java.util.Locale
import com.caverock.androidsvg.SVG
import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import android.text.format.DateFormat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var authRepository: UserRepository
    private var clockJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authRepository = UserRepository.getInstance(requireContext())
    // FAB visibility handled centrally in MainActivity
        setupChart()
        observeViewModel()
        viewModel.load()
        observeUserGreeting()

        // Quick actions removed from Dashboard

        // Load dashboard art from provided SVG in assets
        loadDashboardArtFromSvg("img_meditate_edit.svg", binding.dashboardArt)

        // Initialize date/time immediately
        updateDateTime()
    }

    private fun setupChart() {
        binding.lineChart.apply {
            setNoDataText("")
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 5f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            legend.isEnabled = false
        }
    }

    private fun loadDashboardArtFromSvg(assetName: String, target: ImageView) {
        try {
            requireContext().assets.open(assetName).use { stream ->
                val svg = SVG.getFromInputStream(stream)
                val picture = svg.renderToPicture()
                target.setImageDrawable(PictureDrawable(picture))
                target.clearColorFilter() // ensure no tint applied
            }
        } catch (e: Exception) {
            // Fall back silently; leave any existing src
            e.printStackTrace()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dailyProgress.collect { progress ->
                val pct = progress.completionPercentage.toInt()
                binding.progressCircle.progress = pct
                binding.progressPercentage.text = "$pct%"

                // Stats
                binding.statTotalHabits.text = progress.totalHabits.toString()
                binding.statDoneToday.text = progress.completedHabits.toString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.weeklyMood.collect { data ->
                updateChart(data)
            }
        }
    }

    private fun observeUserGreeting() {
        viewLifecycleOwner.lifecycleScope.launch {
            authRepository.currentUser.collect { user ->
                val base = "Let's have a wonderful day"
                val text = if (user != null && user.name.isNotBlank()) {
                    "Hello ${user.name}!\n$base"
                } else base
                binding.greetingText.text = text
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

        val entries = data.mapIndexed { index, pair -> Entry(index.toFloat(), pair.second) }
        val labels = data.map { it.first }

        val dataSet = LineDataSet(entries, "Mood").apply {
            color = Color.parseColor("#7C4DFF")
            setCircleColor(Color.parseColor("#7C4DFF"))
            lineWidth = 2f
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextColor = Color.TRANSPARENT
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.lineChart.apply {
            setData(LineData(dataSet))
            xAxis.setValueFormatter(com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels))
            xAxis.setLabelRotationAngle(-20f)
            invalidate()
            animateX(600)
        }
    }

    override fun onDestroyView() {
        // FAB visibility handled centrally in MainActivity
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        startClock()
    }

    override fun onPause() {
        super.onPause()
        clockJob?.cancel()
        clockJob = null
    }

    private fun startClock() {
        clockJob?.cancel()
        clockJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                updateDateTime()
                // Update once per minute is enough since we show only date
                val nowMs = System.currentTimeMillis()
                val minuteMs = 60_000L
                val delayMs = minuteMs - (nowMs % minuteMs)
                delay(delayMs)
            }
        }
    }

    private fun updateDateTime() {
        // Show date only
        val pattern = "EEE, MMM d, yyyy"
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        val now = LocalDateTime.now()
        binding.textDateTime.text = now.format(formatter)
    }
}
