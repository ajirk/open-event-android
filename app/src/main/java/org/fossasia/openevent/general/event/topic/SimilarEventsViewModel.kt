package org.fossasia.openevent.general.event.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import timber.log.Timber

class SimilarEventsViewModel(private val eventService: EventService, private val resource: Resource) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableSimilarLocationEvents = MutableLiveData<List<Event>>()
    val similarLocationEvents: LiveData<List<Event>> = mutableSimilarLocationEvents
    private val mutableSimilarIdEvents = MutableLiveData<List<Event>>()
    val similarIdEvents: LiveData<List<Event>> = mutableSimilarIdEvents
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError

    var eventId: Long = -1

    fun loadSimilarIdEvents(id: Long) {
        if (id == -1L) {
            return
        }
        compositeDisposable += eventService.getSimilarEvents(id)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableProgress.value = false
                mutableSimilarIdEvents.value = it.filter { it.id != eventId }
            }, {
                Timber.e(it, "Error fetching similar events")
                mutableError.value = "Error fetching similar events"
            })
    }

    fun loadSimilarLocationEvents(location: String) {

        compositeDisposable += eventService.getEventsByLocation(location)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }
            .doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableSimilarLocationEvents.value = it.filter { it.id != eventId }
            }, {
                Timber.e(it, "Error fetching similar events")
                mutableError.value = resource.getString(R.string.fetch_similar_events_error_message)
            })
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable += eventService.setFavorite(eventId, favorite)
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Success")
            }, {
                Timber.e(it, "Error")
                mutableError.value = resource.getString(R.string.error)
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
