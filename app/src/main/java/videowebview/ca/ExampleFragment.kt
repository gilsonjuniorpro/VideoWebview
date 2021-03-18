package videowebview.ca

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class ExampleFragment(private val s: String) : Fragment(R.layout.fragment_fullscreen){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val someInt = requireArguments().getInt("some_int")
        val t = s

        val btDummy = view.findViewById<Button>(R.id.dummy_button)
        btDummy.setOnClickListener{
            MainActivity.close()
        }
    }
}