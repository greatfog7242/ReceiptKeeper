package com.receiptkeeper.core.util

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.receiptkeeper.core.preferences.IconTheme
import com.receiptkeeper.core.preferences.rememberIconTheme
import java.io.File

/**
 * Utility object for mapping icon names to Material Icons
 */
object IconHelper {

    /**
     * Prefix for brand logos
     */
    const val BRAND_PREFIX = "brand_"

    /**
     * Prefix for custom brand logos (stored in internal storage)
     */
    const val CUSTOM_BRAND_PREFIX = "custom_"

    /**
     * Directory name for custom brand icons in internal storage
     */
    const val CUSTOM_ICONS_DIR = "custom_brand_logos"

    /**
     * List of available brand icon names (without extension)
     */
    val brandIconNames = listOf(
        "ALDI_SÜD", "AsianMarket", "Chick-fil-A", "Chinese_Restaurant", "CITGO", "Costco", "Goodwill_Industries",
        "H_MART", "Kwik_Trip", "Kohls", "Lowes", "Marshalls", "McDonalds", "Meijercom",
        "Menards", "Pick_n_Save", "Piggly_Wiggly", "Pizza_Hut", "QDOBA",
        "Ross_Dress_for_Less", "Sams_Club", "Sendiks", "Target", "The_Home_Depot",
        "TJ_Maxx", "Trader_Joes", "Walgreens", "Walmart"
    )

    /**
     * Check if icon name is a brand icon (either built-in or custom)
     */
    fun isBrandIcon(iconName: String): Boolean {
        return iconName.startsWith(BRAND_PREFIX) ||
               iconName.startsWith(CUSTOM_BRAND_PREFIX) ||
               brandIconNames.contains(iconName) ||
               isCustomIcon(iconName)
    }

    /**
     * Check if icon name is a custom icon
     */
    fun isCustomIcon(iconName: String): Boolean {
        return iconName.startsWith(CUSTOM_BRAND_PREFIX)
    }

    /**
     * Get list of custom brand icons from internal storage
     */
    fun getCustomBrandIcons(context: Context): List<Pair<String, String>> {
        val customDir = File(context.filesDir, CUSTOM_ICONS_DIR)
        if (!customDir.exists()) {
            return emptyList()
        }
        return customDir.listFiles()
            ?.filter { it.extension.lowercase() in listOf("png", "jpg", "jpeg") }
            ?.map { file ->
                val nameWithoutExt = file.nameWithoutExtension
                CUSTOM_BRAND_PREFIX + nameWithoutExt to nameWithoutExt.replace("_", " ")
            }
            ?: emptyList()
    }

    /**
     * Get URI for a custom brand icon
     */
    fun getCustomBrandIconUri(context: Context, iconName: String): String {
        val name = if (iconName.startsWith(CUSTOM_BRAND_PREFIX)) {
            iconName.removePrefix(CUSTOM_BRAND_PREFIX)
        } else {
            iconName
        }
        val customDir = File(context.filesDir, CUSTOM_ICONS_DIR)
        val file = File(customDir, "$name.png")
        return if (file.exists()) {
            "file://${file.absolutePath}"
        } else {
            val jpgFile = File(customDir, "$name.jpg")
            if (jpgFile.exists()) {
                "file://${jpgFile.absolutePath}"
            } else {
                val jpegFile = File(customDir, "$name.jpeg")
                "file://${jpegFile.absolutePath}"
            }
        }
    }

    /**
     * Save a custom brand icon to internal storage
     */
    fun saveCustomBrandIcon(context: Context, iconName: String, imageBytes: ByteArray): Boolean {
        return try {
            val customDir = File(context.filesDir, CUSTOM_ICONS_DIR)
            if (!customDir.exists()) {
                customDir.mkdirs()
            }
            // Clean the name - remove prefix if present
            val cleanName = iconName
                .replace(CUSTOM_BRAND_PREFIX, "")
                .replace(" ", "_")
                .replace(Regex("[^a-zA-Z0-9_]"), "")

            val file = File(customDir, "$cleanName.png")
            file.writeBytes(imageBytes)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get all brand icons (built-in + custom)
     */
    fun getAllBrandIcons(context: Context): List<Pair<String, String>> {
        return getBrandIcons() + getCustomBrandIcons(context)
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
     * Note: This version doesn't use theme - for theme-aware icons, use the composable version
     */
    fun getIcon(iconName: String): ImageVector {
        return iconMap[iconName] ?: Icons.Default.Category
    }

    /**
     * Get ImageVector from icon name with theme support
     * Returns default icon if not found
     * Uses current theme from IconThemeManager
     */
    @Composable
    fun getIconWithTheme(iconName: String): ImageVector {
        val iconTheme = rememberIconTheme().value
        return getIconWithTheme(iconName, iconTheme)
    }

    /**
     * Get ImageVector from icon name with theme support
     * Returns default icon if not found
     */
    fun getIconWithTheme(iconName: String, theme: IconTheme): ImageVector {
        return if (theme == IconTheme.MONOCHROME && isBrandIcon(iconName)) {
            // For monochrome theme, map brand icons to appropriate Material Icons
            getMonochromeFallbackIcon(iconName)
        } else {
            iconMap[iconName] ?: Icons.Default.Category
        }
    }

    /**
     * Map brand icons to Material Icons for monochrome theme
     */
    private fun getMonochromeFallbackIcon(iconName: String): ImageVector {
        val brandName = getBrandIconName(iconName)
        return brandToMaterialIconMap[brandName] ?: iconMap[iconName] ?: Icons.Default.Store
    }

    /**
     * Map brand names to appropriate Material Icons for monochrome theme
     */
    private val brandToMaterialIconMap = mapOf(
        // Grocery stores
        "ALDI_SÜD" to Icons.Default.LocalGroceryStore,
        "AsianMarket" to Icons.Default.LocalGroceryStore,
        "Costco" to Icons.Default.LocalGroceryStore,
        "Goodwill_Industries" to Icons.Default.LocalGroceryStore,
        "H_MART" to Icons.Default.LocalGroceryStore,
        "Kwik_Trip" to Icons.Default.LocalGroceryStore,
        "Kohls" to Icons.Default.LocalMall,
        "Lowes" to Icons.Default.HomeRepairService,
        "Marshalls" to Icons.Default.LocalMall,
        "McDonalds" to Icons.Default.Fastfood,
        "Meijercom" to Icons.Default.LocalGroceryStore,
        "Menards" to Icons.Default.HomeRepairService,
        "Pick_n_Save" to Icons.Default.LocalGroceryStore,
        "Piggly_Wiggly" to Icons.Default.LocalGroceryStore,
        "Pizza_Hut" to Icons.Default.LocalPizza,
        "QDOBA" to Icons.Default.Restaurant,
        "Ross_Dress_for_Less" to Icons.Default.LocalMall,
        "Sams_Club" to Icons.Default.LocalGroceryStore,
        "Sendiks" to Icons.Default.LocalGroceryStore,
        "Target" to Icons.Default.LocalMall,
        "The_Home_Depot" to Icons.Default.HomeRepairService,
        "TJ_Maxx" to Icons.Default.LocalMall,
        "Trader_Joes" to Icons.Default.LocalGroceryStore,
        "Walgreens" to Icons.Default.LocalPharmacy,
        "Walmart" to Icons.Default.LocalGroceryStore,
        
        // Restaurants
        "Chick-fil-A" to Icons.Default.Fastfood,
        "Chinese_Restaurant" to Icons.Default.Restaurant,
        
        // Gas stations
        "CITGO" to Icons.Default.LocalGasStation,
        
        // Default fallback
        "" to Icons.Default.Store
    )

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

    /**
     * Composable for displaying vendor icons with theme support
     */
    @Composable
    fun VendorIcon(
        iconName: String,
        size: Dp,
        tint: Color = Color.Unspecified,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val iconTheme = rememberIconTheme().value

        if (iconTheme == IconTheme.MONOCHROME || !isBrandIcon(iconName)) {
            // For monochrome theme or non-brand icons, use Material Icons
            val materialIcon = getIconWithTheme(iconName, iconTheme)
            Icon(
                imageVector = materialIcon,
                contentDescription = null,
                modifier = modifier.size(size),
                tint = tint
            )
        } else {
            // For colorful theme with brand icons, load the image
            val imageModel = if (isCustomIcon(iconName)) {
                getCustomBrandIconUri(context, iconName)
            } else {
                val brandIconName = getBrandIconName(iconName)
                "file:///android_asset/brand_logos/${brandIconName}.png"
            }
            val model = ImageRequest.Builder(context)
                .data(imageModel)
                .crossfade(true)
                .build()

            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = modifier.size(size),
                contentScale = ContentScale.Fit
            )
        }
    }
}
