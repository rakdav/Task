package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter = CrimeAdapter(emptyList())
    private val taskListViewModel: TaskListViewModel by lazy {
        ViewModelProviders.of(this).get(TaskListViewModel::class.java)
    }
    private var callback: Callbacks? = null

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        callback = context as? Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView =
                view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        return view
    }

    override fun onStart() {
        super.onStart()
        taskListViewModel.taskListLiveData.observe(viewLifecycleOwner,
            Observer { tasks ->
            tasks?.let {
                Log.i(TAG, "Got crimeLiveData ${tasks.size}")
                updateUI(tasks)
            }
        })
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        if (inflater != null) {
            inflater.inflate(R.menu.fragment_task_list,menu)
        };
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
            return  when (item?.itemId) {
                R.id.new_task -> {
                    val task = Task()
                    taskListViewModel.addTask(task)
                    callback?.onCrimeSelected(task.id)
                    true
                }
                else -> return super.onOptionsItemSelected(item)
            }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUI(tasks: List<Task>) {
        adapter?.let {
            it.tasks = tasks
        } ?: run {
            adapter = CrimeAdapter(tasks)
        }
        crimeRecyclerView.adapter = adapter
    }

    private inner class CrimeHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var task: Task

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(task: Task) {
            this.task = task
            titleTextView.text = this.task.title
            dateTextView.text = this.task.date.toString()
            solvedImageView.visibility = if (task.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
            callback?.onCrimeSelected(task.id)
        }
    }

    private inner class CrimeAdapter(var tasks: List<Task>)
        : RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : CrimeHolder {
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = tasks[position]
            holder.bind(crime)
        }

        override fun getItemCount() = tasks.size
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}