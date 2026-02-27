package com.receiptkeeper.core.util

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Utility object for mapping icon names to Material Icons
 */
object IconHelper {

    /**
     * Prefix for brand logos
     */
    const val BRAND_PREFIX = "brand_"

    /**
     * List of available brand icon names (without extension)
     */
    val brandIconNames = listOf(
        "ALDI_SÜD", "Chick-fil-A", "CITGO", "Costco", "Goodwill_Industries",
        "H_MART", "Kwik_Trip", "Lowes", "Marshalls", "McDonalds", "Meijercom",
        "Menards", "Pick_n_Save", "Piggly_Wiggly", "Pizza_Hut", "QDOBA",
        "Ross_Dress_for_Less", "Sams_Club", "Sendiks", "Target", "The_Home_Depot",
        "TJ_Maxx", "Trader_Joes", "Walgreens", "Walmart"
    )

    /**
     * Check if icon name is a brand icon
     */
    fun isBrandIcon(iconName: String): Boolean {
        return iconName.startsWith(BRAND_PREFIX) || brandIconNames.contains(iconName)
    }

    /**
     * Get brand icon resource name
     */
    fun getBrandIconName(iconName: String): String {
        return if (iconName.startsWith(BRAND_PREFIX)) {
            iconName.removePrefix(BRAND_PREFIX)
        } else {
            iconName
        }
    }

    /**
     * Map of icon names to ImageVector icons
     */
    private val iconMap = mapOf(
        // General
        "Category" to Icons.Default.Category,
        "Store" to Icons.Default.Store,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "AccountCircle" to Icons.Default.AccountCircle,
        "Home" to Icons.Default.Home,
        "Star" to Icons.Default.Star,
        "Favorite" to Icons.Default.Favorite,
        "MoreVert" to Icons.Default.MoreVert,

        // Food & Dining
        "Restaurant" to Icons.Default.Restaurant,
        "Fastfood" to Icons.Default.Fastfood,
        "LocalCafe" to Icons.Default.LocalCafe,
        "LocalBar" to Icons.Default.LocalBar,
        "BakeryDining" to Icons.Default.BakeryDining,
        "LunchDining" to Icons.Default.LunchDining,
        "DinnerDining" to Icons.Default.DinnerDining,
        "BreakfastDining" to Icons.Default.BreakfastDining,
        "Icecream" to Icons.Default.Icecream,
        "LocalPizza" to Icons.Default.LocalPizza,
        "Kitchen" to Icons.Default.Kitchen,

        // Shopping
        "LocalMall" to Icons.Default.LocalMall,
        "LocalGroceryStore" to Icons.Default.LocalGroceryStore,
        "Shop" to Icons.Default.Shop,
        "ShopTwo" to Icons.Default.ShopTwo,
        "Storefront" to Icons.Default.Storefront,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "Checkroom" to Icons.Default.Checkroom,

        // Healthcare
        "LocalHospital" to Icons.Default.LocalHospital,
        "MedicalServices" to Icons.Default.MedicalServices,
        "HealthAndSafety" to Icons.Default.HealthAndSafety,
        "Emergency" to Icons.Default.Emergency,
        "MedicalInformation" to Icons.Default.MedicalInformation,

        // Transportation
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "LocalParking" to Icons.Default.LocalParking,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "CarRental" to Icons.Default.CarRental,
        "Train" to Icons.Default.Train,
        "Flight" to Icons.Default.Flight,
        "DirectionsBus" to Icons.Default.DirectionsBus,
        "TwoWheeler" to Icons.Default.TwoWheeler,
        "ElectricCar" to Icons.Default.ElectricCar,
        "EvStation" to Icons.Default.EvStation,

        // Entertainment
        "Movie" to Icons.Default.Movie,
        "Sports" to Icons.Default.Sports,
        "Casino" to Icons.Default.Casino,
        "MusicNote" to Icons.Default.MusicNote,
        "TheaterComedy" to Icons.Default.TheaterComedy,
        "Games" to Icons.Default.Games,
        "SportsEsports" to Icons.Default.SportsEsports,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "Pool" to Icons.Default.Pool,
        "GolfCourse" to Icons.Default.GolfCourse,

        // Utilities
        "Phone" to Icons.Default.Phone,
        "Wifi" to Icons.Default.Wifi,
        "Power" to Icons.Default.Power,
        "WaterDrop" to Icons.Default.WaterDrop,
        "HomeRepairService" to Icons.Default.HomeRepairService,
        "Build" to Icons.Default.Build,
        "ElectricalServices" to Icons.Default.ElectricalServices,
        "Plumbing" to Icons.Default.Plumbing,
        "CleaningServices" to Icons.Default.CleaningServices,

        // Finance
        "AccountBalance" to Icons.Default.AccountBalance,
        "CreditCard" to Icons.Default.CreditCard,
        "Money" to Icons.Default.Money,
        "Payments" to Icons.Default.Payments,
        "Savings" to Icons.Default.Savings,
        "TrendingUp" to Icons.Default.TrendingUp,

        // Education
        "School" to Icons.Default.School,
        "Book" to Icons.Default.Book,
        "LibraryBooks" to Icons.Default.LibraryBooks,
        "MenuBook" to Icons.Default.MenuBook,
        "Computer" to Icons.Default.Computer,

        // Travel
        "Hotel" to Icons.Default.Hotel,
        "BeachAccess" to Icons.Default.BeachAccess,
        "Pool" to Icons.Default.Pool,
        "Landscape" to Icons.Default.Landscape,
        "FlightTakeoff" to Icons.Default.FlightTakeoff,
        "FlightLand" to Icons.Default.FlightLand,
        "Luggage" to Icons.Default.Luggage,

        // Other
        "Receipt" to Icons.Default.Receipt,
        "AttachMoney" to Icons.Default.AttachMoney,
        "Sell" to Icons.Default.Sell,
        "LocalOffer" to Icons.Default.LocalOffer,
        "PointOfSale" to Icons.Default.PointOfSale,
        "Inventory" to Icons.Default.Inventory,
        "Style" to Icons.Default.Style,
        "Pets" to Icons.Default.Pets,
        "ChildCare" to Icons.Default.ChildCare,
        "ChildFriendly" to Icons.Default.ChildFriendly,
        "Elderly" to Icons.Default.Elderly,
        "SmokingRooms" to Icons.Default.SmokingRooms,
        "Weekend" to Icons.Default.Weekend
    )

    /**
     * Get ImageVector from icon name
     * Returns default icon if not found
     */
    fun getIcon(iconName: String): ImageVector {
        return iconMap[iconName] ?: Icons.Default.Category
    }

    /**
     * Get all available icon names
     */
    fun getAvailableIcons(): List<Pair<String, ImageVector>> {
        return iconMap.toList().sortedBy { it.first }
    }

    /**
     * Get icon names grouped by category for display
     */
    fun getIconCategories(): Map<String, List<Pair<String, ImageVector>>> {
        val categories = mutableMapOf<String, MutableList<Pair<String, ImageVector>>>()

        val general = listOf("Category", "Store", "ShoppingCart", "AccountCircle", "Home", "Star", "Favorite", "MoreVert")
        val food = listOf("Restaurant", "Fastfood", "LocalCafe", "LocalBar", "BakeryDining", "LunchDining", "DinnerDining", "BreakfastDining", "Icecream", "LocalPizza", "Kitchen")
        val shopping = listOf("LocalMall", "LocalGroceryStore", "Shop", "ShopTwo", "Storefront", "ShoppingBag", "Checkroom")
        val healthcare = listOf("LocalHospital", "MedicalServices", "Pharmacy", "HealthAndSafety", "LocalDoctor", "Vaccines", "MedicalInformation")
        val transportation = listOf("LocalGasStation", "LocalParking", "DirectionsCar", "CarRental", "Train", "Flight", "DirectionsBus", "TwoWheeler", "ElectricCar", "EvStation")
        val entertainment = listOf("Movie", "Sports", "Casino", "MusicNote", "TheaterComedy", "Games", "SportsEsports", "FitnessCenter", "Pool", "GolfCourse")
        val utilities = listOf("Phone", "Wifi", "Power", "WaterDrop", "HomeRepairService", "Build", "ElectricalServices", "Plumbing", "CleaningServices")
        val finance = listOf("AccountBalance", "CreditCard", "Money", "Payments", "Savings", "TrendingUp")
        val education = listOf("School", "Book", "LibraryBooks", "MenuBook", "Computer")
        val travel = listOf("Hotel", "BeachAccess", "Landscape", "FlightTakeoff", "FlightLand", "Luggage")
        val other = listOf("Receipt", "AttachMoney", "Sell", "LocalOffer", "PointOfSale", "Inventory", "Style", "Pets", "ChildCare", "ChildFriendly", "Elderly", "Glass", "SmokeFree", "Weekend")

        fun addToCategory(name: String, category: String) {
            iconMap[name]?.let {
                categories.getOrPut(category) { mutableListOf() }.add(name to it)
            }
        }

        general.forEach { addToCategory(it, "General") }
        food.forEach { addToCategory(it, "Food & Dining") }
        shopping.forEach { addToCategory(it, "Shopping") }
        healthcare.forEach { addToCategory(it, "Healthcare") }
        transportation.forEach { addToCategory(it, "Transportation") }
        entertainment.forEach { addToCategory(it, "Entertainment") }
        utilities.forEach { addToCategory(it, "Utilities") }
        finance.forEach { addToCategory(it, "Finance") }
        education.forEach { addToCategory(it, "Education") }
        travel.forEach { addToCategory(it, "Travel") }
        other.forEach { addToCategory(it, "Other") }

        return categories
    }

    /**
     * Get all available brand icons as pairs (name, display name)
     */
    fun getBrandIcons(): List<Pair<String, String>> {
        return brandIconNames.map { it to it.replace("_", " ").replace("'", "'") }
    }
}
