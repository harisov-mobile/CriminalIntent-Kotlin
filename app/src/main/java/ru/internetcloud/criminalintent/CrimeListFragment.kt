package ru.internetcloud.criminalintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.internetcloud.criminalintent.model.Crime
import ru.internetcloud.criminalintent.model.CrimeListViewModel
import java.text.SimpleDateFormat

private const val TAG = "rustam"
private const val DATE_FORMAT = "dd MMMM yyyy, EEEE, HH : mm"
// день Месяц словом, год 4 знака, день недели 2 буквы: // https://javarush.ipnodns.ru/lesson/lect40.html
private val sdf = SimpleDateFormat(DATE_FORMAT)

class CrimeListFragment: Fragment() {

    // свойства класса: (с отложенной инициализацией)
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    private lateinit var crimeListRecyclerView: RecyclerView
    private var crimeListAdapter: CrimeAdapter? = CrimeAdapter(emptyList())

    // статика
    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeListRecyclerView = view.findViewById(R.id.crime_list_recycler_view) as RecyclerView
        crimeListRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeListRecyclerView.adapter = crimeListAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes -> crimes?.let {
                Log.i(TAG, "Got crimes ${crimes.size}")
                updateUI(crimes)
            } }
        )
    }


    // внутренний класс CrimeHolder
    private inner class CrimeHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved_image_view)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = crime.title

            dateTextView.text = sdf.format(crime.date)
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(p0: View?) {
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
        }

    }

    // внутренний класс адаптер
    private inner class CrimeAdapter(var crimes: List<Crime>): RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val itemView = layoutInflater.inflate(R.layout.list_item_crime3, parent, false)
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

    private fun updateUI(crimes: List<Crime>) {
        crimeListAdapter = CrimeAdapter(crimes)
        crimeListRecyclerView.adapter = crimeListAdapter
    }
}