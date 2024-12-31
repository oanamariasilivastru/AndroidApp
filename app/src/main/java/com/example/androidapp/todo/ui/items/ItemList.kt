// com/example/androidapp/todo/ui/items/ItemList.kt

package com.example.androidapp.todo.ui.items

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.androidapp.R
import com.example.androidapp.todo.data.Product

typealias OnItemFn = (id: String?) -> Unit

@Composable
fun ItemList(
    itemList: List<Product>,
    onItemClick: OnItemFn,
    modifier: Modifier = Modifier
) {
    if (itemList.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nu există produse disponibile",
                style = TextStyle(fontSize = 18.sp)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            items(itemList) { item ->
                ItemDetail(item, onItemClick)
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ItemDetail(
    item: Product,
    onItemClick: OnItemFn,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(fontSize = 16.sp)
) {
    // Definirea priceFormatter la începutul composable-ului pentru a fi accesibil în toate secțiunile
    val priceFormatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("ro", "RO"))

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Zona navigabilă (Column clicabilă)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            Log.d("ItemDetail", "Navigating to item: ${item._id}")
                            onItemClick(item._id)
                        }
                ) {
                    // Afișarea imaginii dacă imageUri este disponibilă
                    if (!item.imageUri.isNullOrEmpty()) {
                        Image(
                            painter = rememberImagePainter(
                                data = item.imageUri,
                                builder = {
                                    crossfade(true)
                                    placeholder(R.drawable.placeholder) // Asigură-te că ai un placeholder în resurse
                                    error(R.drawable.error) // Asigură-te că ai un drawable de eroare
                                }
                            ),
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp) // Ajustează dimensiunea după necesități
                                .padding(bottom = 8.dp)
                        )
                    }

                    // Nume produs
                    Text(
                        text = item.name,
                        style = textStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )

//                    // Categorie
//                    Text(
//                        text = "Categorie: ${item.category}",
//                        style = textStyle,
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
//
//                    // Preț
//                    Text(
//                        text = "Preț: ${priceFormatter.format(item.price)} RON",
//                        style = textStyle,
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
//
//                    // Disponibilitate
//                    Text(
//                        text = if (item.inStock) "În stoc" else "Stoc epuizat",
//                        style = textStyle.copy(color = if (item.inStock) Color.Green else Color.Red),
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
                }

                // Icon pentru expansiune/collapsare, clicabil independent
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp)
                        .clickable {
                            Log.d("ItemDetail", "Toggling expand state for item: ${item._id}")
                            isExpanded = !isExpanded
                        }
                )
            }

            // Opțional: ID-ul produsului
            item._id?.let { id ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: $id",
                    style = textStyle.copy(fontSize = 12.sp, color = Color.Gray)
                )
            }

            // Conținut colapsabil
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)) + expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)) + shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Detalii suplimentare despre produs
                    Text(
                        text = "ID: ${item._id}",
                        style = textStyle.copy(fontSize = 14.sp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Categorie: ${item.category}",
                        style = textStyle.copy(fontSize = 14.sp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Preț: ${priceFormatter.format(item.price)} RON",
                        style = textStyle.copy(fontSize = 14.sp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Disponibilitate: ${if (item.inStock) "În stoc" else "Stoc epuizat"}",
                        style = textStyle.copy(fontSize = 14.sp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
//                    // Conținut suplimentar static
//                    Text(
//                        text = "Detalii suplimentare: Poți adăuga mai multe informații aici.",
//                        style = textStyle.copy(fontSize = 14.sp),
//                        modifier = Modifier.padding(bottom = 4.dp)
//                    )
                }
            }
        }
    }
}
