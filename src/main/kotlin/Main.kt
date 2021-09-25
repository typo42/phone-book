package phonebook

import java.io.File
import kotlin.math.sqrt
import kotlin.system.exitProcess

fun timeLapsed(timeMs: Long): String {
    val m = (timeMs / 60000 % 60).toInt().toString()
    var s = (timeMs / 1000 % 60).toInt().toString()
    if (s.length < 2) s = "0$s"
    var ms = (timeMs % 1000).toInt().toString()
    while (ms.length < 3) ms = "0$ms"
    return "$m min. $s sec. $ms ms."
}

fun dropUntilName(directoryEntry: String) = run { directoryEntry.dropWhile { (it.isDigit() || (it == ' ')) } }

fun linearSearch(find: List<String>, directory: List<String>): Pair<Int, Long> {
    var matches = 0
    val startLinearSearchTime = System.currentTimeMillis()
    find.forEach {
        for (n in 0..directory.lastIndex) {
            if (it == dropUntilName(directory[n])) matches++
            continue
        }
    }
    val linearSearchTime = System.currentTimeMillis() - startLinearSearchTime
    return Pair(matches, linearSearchTime)
}

fun bubbleSort(list: MutableList<String>, startBubbleSortingTime: Long, linearSearchTime: Long): MutableList<String>? {
    var swap = true
    while (swap) {
        swap = false
        for (n in 0 until list.size - 1) {
            // this algorithm is extremely slow and is normally skipped, comment out below to allow full run:
            if (System.currentTimeMillis() - startBubbleSortingTime > linearSearchTime * 3) return null //
            if (dropUntilName(list[n]) > dropUntilName(list[n + 1])) {
                list.add(n, list.removeAt(n + 1))
                swap = true
            }
        }
    }
    return list
}

fun jumpSearch(line: String, sortedDirectory: List<String>): Boolean {
    val step = sqrt(sortedDirectory.size.toDouble()).toInt()
    var n = 0
    if (line == dropUntilName(sortedDirectory[n])) return true
    n = 1
    while (n <= sortedDirectory.lastIndex - step) {
        n += step
        if (line == dropUntilName(sortedDirectory[n])) {
            return true
        } else if (line < dropUntilName(sortedDirectory[n])) {
            for (i in n - 1 downTo n - step) {
                n--
                if (line == dropUntilName(sortedDirectory[i])) {
                    return true
                }
            }
        }
    }
    for (i in 1 until sortedDirectory.size - step) {
        if (line == dropUntilName(sortedDirectory[n + i])) {
            return true
        }
    }
    return false
}

fun quickSort(directory: List<String>): List<String>{
    if (directory.count() < 2){
        return directory
    }
    val pivot = directory[directory.count() / 2].dropWhile { (it.isDigit() || (it == ' ')) }
    val equal = directory.filter { dropUntilName(it) == pivot }
    val less = directory.filter { dropUntilName(it) < pivot }
    val greater = directory.filter { dropUntilName(it) > pivot }

    return quickSort(less) + equal + quickSort(greater)
}

var matches = 0

fun main() {

//    val find = File("C:\\app\\JetBrains\\phone book data\\tiny_find.txt").readLines()
//    var directory = File("C:\\app\\JetBrains\\phone book data\\tiny_directory.txt").readLines()

//    val find = File("C:\\app\\JetBrains\\phone book data\\small_find.txt").readLines()
//    var directory = File("C:\\app\\JetBrains\\phone book data\\small_directory.txt").readLines()

//    val find = File("C:\\app\\JetBrains\\phone book data\\medium_find.txt").readLines()
//    var directory = File("C:\\app\\JetBrains\\phone book data\\medium_directory.txt").readLines()

    val find = File("C:\\app\\JetBrains\\phone book data\\find.txt").readLines()
    var directory = File("C:\\app\\JetBrains\\phone book data\\directory.txt").readLines()

    println("Start searching (linear search)...")
    var (matches, linearSearchTime) = linearSearch(find, directory)
    println("Found $matches / ${find.size} entries. Time taken: ${timeLapsed(linearSearchTime)}")

    matches = 0
    println("\nStart searching (bubble sort + jump search)...")
    val startBubbleSortTime = System.currentTimeMillis()
    val bubbleSortResult = bubbleSort(directory.toMutableList(), startBubbleSortTime, linearSearchTime)
    val bubbleSortTime = System.currentTimeMillis() - startBubbleSortTime

    fun hashTableSearch(find: List<String>, directory: List<String>) {
        println("\nStart searching (hash table)...")
        val startCreatingTime = System.currentTimeMillis()
        val directoryMap = hashMapOf<String, String>()
        for (line in directory) {
            val number = line.dropLastWhile { !it.isDigit() || it == ' ' }
            val name = dropUntilName(line)
            directoryMap[name] = number
        }
        val creatingTime = System.currentTimeMillis() - startCreatingTime
        matches = 0
        val startMapSearchTime = System.currentTimeMillis()
        for (entry in find) {
            if (directoryMap.containsKey(entry)) matches++
        }
        val mapSearchTime = System.currentTimeMillis() - startMapSearchTime
        println("Found $matches / ${find.size} entries. Time taken: ${timeLapsed(creatingTime + mapSearchTime)}")
        println("Creating time: ${timeLapsed(creatingTime)}")
        println("Searching time: ${timeLapsed(mapSearchTime)}")
        exitProcess(0)
    }


    fun quickSortBinarySearch() {
        println("\nStart searching (quick sort + binary search)...")
        val quickSortStartTime = System.currentTimeMillis()
        val directoryQuickSorted = quickSort(directory)
        val quickSortTime = System.currentTimeMillis() - quickSortStartTime
        // workaround: cannot perform binary search in directory when numbers are present
        val directoryDropNumbers = directoryQuickSorted.map { dropUntilName(it)}
        val binarySearchStart = System.currentTimeMillis()
        matches = 0
        find.forEach {  if (directoryDropNumbers.binarySearch(it) > 0) matches++ }
        val binarySearchTime = System.currentTimeMillis() - binarySearchStart
        println("Found $matches / ${find.size} entries. Time taken: ${timeLapsed(quickSortTime + binarySearchTime)}")
        println("Sorting time: ${timeLapsed(quickSortTime)}")
        println("Searching time: ${timeLapsed(binarySearchTime)}")
        hashTableSearch(find, directory)
    }

    fun backupScenario() {
        val (matchesBackup, linearSearchTimeBackup) = linearSearch(find, directory)
        println("Found $matchesBackup / ${find.size} entries. Time taken: ${timeLapsed(linearSearchTimeBackup + bubbleSortTime)}")
        println("Sorting time: ${timeLapsed(bubbleSortTime)} - STOPPED, moved to linear search")
        println("Searching time: ${timeLapsed(linearSearchTimeBackup)}")
        quickSortBinarySearch()
    }

    if (!bubbleSortResult.isNullOrEmpty()) {
        directory = bubbleSortResult
    } else {
        backupScenario()
    }

    val startJumpSearchTime = System.currentTimeMillis()
    find.forEach { if (jumpSearch(it, directory)) matches++ }
    val jumpSearchTime = System.currentTimeMillis() - startJumpSearchTime
    println("Found $matches / ${find.size} entries. Time taken: ${timeLapsed(bubbleSortTime + jumpSearchTime)}")
    println("Sorting time: ${timeLapsed(bubbleSortTime)}")
    println("Searching time: ${timeLapsed(jumpSearchTime)}")

    quickSortBinarySearch()
}
