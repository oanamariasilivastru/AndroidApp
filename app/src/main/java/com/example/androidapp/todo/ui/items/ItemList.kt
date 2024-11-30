package com.example.androidapp.todo.ui.items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidapp.todo.data.Product

typealias OnItemFn = (id: String?) -> Unit

@Composable
fun ItemList(
    itemList: List<Product>,
    onItemClick: OnItemFn,
    modifier: Modifier = Modifier
) {
    if (itemList.isEmpty()) {
        Text(
            text = "Nu există produse disponibile",
            modifier = modifier.padding(16.dp),
            style = TextStyle(fontSize = 18.sp)
        )
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

@Composable
fun ItemDetail(
    item: Product,
    onItemClick: OnItemFn,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(fontSize = 16.sp)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Nume (clicabil)
        ClickableText(
            text = AnnotatedString(item.name),
            style = textStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            onClick = {
                if (!item._id.isNullOrEmpty()) {
                    onItemClick(item._id)
                } else {
                    // Opțional: Gestionați ID-urile nevalide
                }
            }
        )

        // Categorie
        Text(
            text = "Categorie: ${item.category}",
            style = textStyle,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Preț
        val priceFormatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("ro", "RO"))
        Text(
            text = "Preț: ${priceFormatter.format(item.price)} RON",
            style = textStyle,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Disponibilitate
        Text(
            text = if (item.inStock) "În stoc" else "Stoc epuizat",
            style = textStyle,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Opțional: Afișați ID-ul (dacă este necesar)
        item._id?.let { id ->
            Text(
                text = "ID: $id",
                style = textStyle.copy(fontSize = 12.sp, color = Color.Gray),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
