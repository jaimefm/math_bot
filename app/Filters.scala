import javax.inject._
import com.digitaltangible.playguard.GuardFilter
import play.api._
import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter

@Singleton
class Filters @Inject()(env: Environment, guardFilter: GuardFilter, CORSFilter: CORSFilter)
    extends DefaultHttpFilters(CORSFilter)
