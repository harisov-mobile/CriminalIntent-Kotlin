package ru.internetcloud.criminalintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.internetcloud.criminalintent.model.Crime
import ru.internetcloud.criminalintent.model.CrimeListViewModel

private const val TAG = "rustam"

class CrimeListFragment: Fragment() {

    // свойства класса: (с отложенной инициализацией)
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    private lateinit var crimeListRecyclerView: RecyclerView
    private var crimeListAdapter: CrimeAdapter? = null

    // статика
    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "Total crimes: ${crimeListViewModel.crimes.size}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeListRecyclerView = view.findViewById(R.id.crime_list_recycler_view) as RecyclerView
        crimeListRecyclerView.layoutManager = LinearLayoutManager(context)

        updateUI()

        return view
    }

    // внутренний класс CrimeHolder
    private inner class CrimeHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private lateinit var crime: Crime

        val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        val dateTextView: TextView = itemView.findViewById(R.id.crime_date)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = crime.title
            dateTextView.text = crime.date.toString()
        }

        override fun onClick(p0: View?) {
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
        }

    }

    // внутренний класс адаптер
    private inner class CrimeAdapter(var crimes: List<Crime>): RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val itemView = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(itemView)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemCount(): Int {
            return crimes.size
        }

    }

    private fun updateUI() {
        val crimes = crimeListViewModel.crimes
        crimeListAdapter = CrimeAdapter(crimes)
        crimeListRecyclerView.adapter = crimeListAdapter
    }
}