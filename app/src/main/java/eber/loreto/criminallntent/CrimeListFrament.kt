package eber.loreto.criminallntent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import import kotlinx.coroutines.flow.collect

class CrimeListFrament:Frament() {

    private var _binding: FragmentCrimeDetailBinding? = null
    private val binding
        get() = checkNotBull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val crimeListViewModel: CrimeListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): view?{
        _binding = FragmentCrimeDetailBinding.inflate(inflater, container, false)
        binding.crimeRecyclerView.layoutManager = LinearLayoutManager (context)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //val crimes = crimeListViewModel.loadCrimes()
                crimeListViewModel.crimes.collect { crimes ->
                binding.crimeRecyclerView.adapter =
                    CrimeListAdapter(crimes) { crimeId ->
                    findNavController().navigate(
                        //R.id.show_crime_detail
                        CrimeListFragmentDirections.showCrimeDetail(crimeId)
                    )
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            super.onCreateOptionsMenu(menu, inflater)
            inflater.inflate(R.menu.fragment_crime_list, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.new_crime -> {
                    showNewCrime()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        private fun showNewCrime() {
            viewLifecycleOwner.lifecycleScope.launch {
                val newCrime = Crime(
                    id = UUID.randomUUID(),
                    title = "",
                    date = Date(),
                    isSolved = false
                )
                crimeListViewModel.addCrime(newCrime)
                findNavController().navigate(
                    CrimeListFragmentDirections.showCrimeDetail(newCrime.id)
                )
            }
        }

}