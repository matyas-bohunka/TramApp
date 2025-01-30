package com.example.tramapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tramapp.databinding.ItemMeasurementBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeasurementAdapter(private val onItemClicked: (Measurement) -> Unit) :
    ListAdapter<Measurement, MeasurementAdapter.MeasurementViewHolder>(MeasurementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val binding = ItemMeasurementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MeasurementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        val currentMeasurement = getItem(position)
        holder.bind(currentMeasurement)
    }

    inner class MeasurementViewHolder(private val binding: ItemMeasurementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(measurement: Measurement) {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(measurement.date))
            binding.textViewMeasurementInfo.text = "${measurement.trackNumber} - $formattedDate"
            binding.root.setOnClickListener {
                onItemClicked(measurement)
            }
        }
    }

    class MeasurementDiffCallback : DiffUtil.ItemCallback<Measurement>() {
        override fun areItemsTheSame(oldItem: Measurement, newItem: Measurement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Measurement, newItem: Measurement): Boolean {
            return oldItem == newItem
        }
    }
}