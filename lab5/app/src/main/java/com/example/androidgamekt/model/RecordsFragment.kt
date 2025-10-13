package com.example.androidgamekt.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidgamekt.R
import com.example.androidgamekt.data.AppDatabase
import com.example.androidgamekt.data.entity.RecordEntity
import com.example.androidgamekt.data.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.rvRecords)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RecordsAdapter()
        recycler.adapter = adapter
        loadRecords(adapter)
    }

    override fun onResume() {
        super.onResume()
        val recycler = view?.findViewById<RecyclerView>(R.id.rvRecords) ?: return
        val adapter = recycler.adapter as? RecordsAdapter ?: return
        loadRecords(adapter)
    }

    private fun loadRecords(adapter: RecordsAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(requireContext())
                val records = db.recordDao().getAllOrdered()
                val userMap: Map<Long, UserEntity> = db.userDao().getAll().associateBy { it.id }
                records.map { rec ->
                    Triple(userMap[rec.userId]?.fullName ?: "?", rec.score, rec.difficulty)
                }
            }
            adapter.submit(items)
        }
    }
}

private class RecordsAdapter : RecyclerView.Adapter<RecordVH>() {
    private val data = mutableListOf<Triple<String, Int, String>>()
    fun submit(items: List<Triple<String, Int, String>>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordVH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecordVH, position: Int) {
        val (name, score, difficulty) = data[position]
        holder.bind(name, score, difficulty)
    }
}

private class RecordVH(view: View) : RecyclerView.ViewHolder(view) {
    private val tvName = view.findViewById<TextView>(R.id.tvName)
    private val tvScore = view.findViewById<TextView>(R.id.tvScore)
    private val tvDifficulty = view.findViewById<TextView>(R.id.tvDifficulty)

    fun bind(name: String, score: Int, difficulty: String) {
        tvName.text = name
        tvScore.text = score.toString()
        tvDifficulty.text = difficulty
    }
}


