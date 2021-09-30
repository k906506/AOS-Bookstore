# 키워드

- RecyclerView
- View Binding
- Retrofit
- Glide
- Android Room
- Open API

# 구현 목록

1. Retrofit 라이브러리로 API 호출
2. ListAdapter로 RecyclerView 구현

# 개발 과정

## 1. Open API 연동하기

이번 앱은 인터파크의 open api를 사용해서 도서 목록을 불러오려고 한다. 우선 `postman` 을 사용해서 `json` 으로 넘겨주는 데이터의 형식을 보자.

![](https://images.velog.io/images/k906506/post/070d1515-2c60-491e-b9aa-8d0c1f8754d3/image.png)

하지만 json으로 넘겨주므로 string으로 받아오게 되고 형변환을 무조건 해줘야하는 번거로움이 있다. 수고를 조금 덜기 위해 object 타입으로 가져오는 gson으로 바로 변환해주는 라이브러리도 추가해줬다.

![](https://images.velog.io/images/k906506/post/4641f4ed-8843-448a-bb64-208bcab081e2/image.png)

`gson` 라이브러리를 추가하면 `@SerializedName` 키워드를 통해 바로 변환을 해줄 수 있다.

```kotlin
package com.example.bookstore.model

import com.google.gson.annotations.SerializedName

data class BestSellerDTO(
    @SerializedName("title") val id: String,
    @SerializedName("item") val books: List<Book>,
)

// 또 다른 클래스
package com.example.bookstore.model

import com.google.gson.annotations.SerializedName

data class SearchBookDTO(
    @SerializedName("title") val id: String,
    @SerializedName("item") val books: List<Book>,
)

```

위에 `postman` 을 보면 다양한 `key - value` 를 넘겨준다. 그 중에서 `title` 에는 베스트셀러가 `item` 에는 베스트셀러에 해당하는 도서 목록이 담겨 있다. 이를 `@SerializedName` 키워드를 통해 바로 변환해준다.

```kotlin
package com.example.bookstore.api

import com.example.bookstore.model.BestSellerDTO
import com.example.bookstore.model.SearchBookDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookService {
    @GET("/api/search.api?output=json")
    fun getBooksByName(
        @Query("key") apiKey : String,
        @Query("query") keyword: String
    ) : Call<SearchBookDTO> // SearchBookDTO를 호출

    @GET("/api/bestSeller.api?output=json&categoryId=100")
    fun getBestSellerBooks(
        @Query("key") apiKey: String,
    ) : Call<BestSellerDTO> // BestSellerDTO를 호출
}
```

API 연동을 확인하기 위해 앱을 실행했는데 아래와 같은 오류가 뜨면서 앱이 바로 죽어버린다.

![](https://images.velog.io/images/k906506/post/ad6e35d9-055d-4849-aa32-b5cc58505666/image.png)

읽어보면 인터넷 권한을 아직 설정해주지 않았다는 오류. 따라서 `Manifests` 에서 인터넷 사용 권한을 추가해줬다. 앱을 다시 실행해보면 정상적으로 `Logcat` 에 출력되는 것을 볼 수 있다. 

![](https://images.velog.io/images/k906506/post/066207c8-7f14-4106-a153-1a8b73426c67/image.png)

성공!

## 2. Recycler View 구현하기

지난번에 "오늘의 명언 앱"을 구현할 때 사용했던 `Recycler View` 를 이번에도 사용할 것이다. `Recycler View` 를 사용할 때 필요한 것은 총 3가지가 있다. 

- 뷰를 화면에 그려주는 `onCreateViewHolder`
- 데이터를 뷰에 연결해주는 `onBindViewHolder`
- 데이터의 개수를 알려주는 `getItemCount`

이를 `Adapter` 라고 하는데 이렇게 총 3가지를 `오버라이딩` 해줘야 한다. 하지만 이번엔 조금 다른 방법으로 접근하려고 한다. 우리가 `Recycler View` 로 구현하려는 것은 도서 목록이다. 즉, 리스트 형태로 표시하려고 하는데 이를 좀 더 효율적으로 표시할 수 있게 하는 `ListAdapter` 를 사용할 것이다. `ListAdapter` 를 사용하면 왜 효율적인지는 아래에서 설명하겠다. 

### 기존 방법의 ListView

새로운 도서 목록을 보여주려고 한다. 이 때 도서 목록이 담겨 있는 리스트에 새로운 도서를 추가하고 뷰에 보여주기 위해서는 뷰에 "새로운 값이 담겼어" 라는 말을 전달해야한다. 이는 `notifyDataSetChanged` 메소드를 호출해서 알려주는데 이 때 데이터의 양의 엄청 많은 경우 렌더링 지연이 발생한다. 이를 해결하기 위해 구글에서는 매우 획기적인 `DiffUtil` 클래스를 만들어줬다.

### DiffUtil

이 클래스는 리스트에서 변경이 있는 데이터만 바꿔준다. 클래스 내부에서 기존 아이템과 새로운 아이템을 비교하고 새로운 아이템만 바꿔주는 기능을 수행한다. `ItemCallback` 메소드가 추상 클래스여서 object로 선언해서 정의했다. 제네릭 타입으로 값을 비교할 아이템의 타입을 넣어주면 해당 아이템을 비교한다. 

```kotlin
	companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }
```
### Adapter 구현

위에서 말했듯 효율적으로 출력하기 위해 `ListAdapter` 를 상속받아서 구현했다. `Inner class` 로 ViewHolder을 구현했다. 이 때 `Viewbinding` 을 사용했는데 기존에는 `findByViewId` 로 뷰와 연결해줬다면 이번엔 `Viewbinding` 으로 구현했다. 

```kotlin
package com.example.bookstore.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.databinding.ItemBookBinding
import com.example.bookstore.model.Book

class BookAdapter : ListAdapter<Book, BookAdapter.BookItemViewHolder>(diffUtil) {

    inner class BookItemViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bookModel: Book) {
            binding.titleTextView.text = bookModel.title
        }
    }

    // 뷰가 없을경우 뷰를 그려주는 메소드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemViewHolder {
        return BookItemViewHolder(
            ItemBookBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    // 뷰에 데이터를 넣어주는 bind 메소드
    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }

}
```

`Adapter` 를 구현했으니 이제 뷰와 연결해주면 된다.

```kotlin
	private fun initBookRecyclerView() {
        adapter = BookAdapter()
        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = adapter
    }
```
## 3. 도서 목록 뷰 수정하기

기존의 도서 목록을 보여주는 레이아웃의 경우 API가 정상적으로 호출되는지 보기 위해 단순하게 `title` 만 출력하는 것을 정해줬었다. 이제 실제 UI를 그려줘야한다. `item_book` 을 추가해서 작업을 진행했다. 이 레이아웃은 보여질 도서 목록에서 도서 한 권에 해당하는 레이아웃이다.

```kotlin
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp">

    <ImageView
        android:id="@+id/coverImageView"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:background="@drawable/background_gray_stroke_radius_16"
        android:padding="8dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/titleTextView"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="12dp"
        android:ellipsize="end"
        android:lines="1"
        android:textColor="@color/black"
        android:textSize="16sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@+id/coverImageView"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/descriptionTextView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="12dp"
        android:ellipsize="end"
        android:maxLines="3"
        android:textSize="12sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="@id/titleTextView"
        app:layout_constraintTop_toBottomOf="@itleTextView" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

도서 사진을 표시할 `ImageView` 와 도서 제목을 표시할 `TextView` , 도서 설명을 표시할 `TextView` , 총 3개의 뷰로 만들어줬다. 이미지의 경우 그냥 단순히 사진을 보여주면 밋밋할 것 같아서 `drawable` 로 백그라운드 레이아웃을 추가해줬다.

```kotlin
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <stroke
        android:width="2dp"
        android:color="@color/gray" />

    <corners android:radius="16dp" />

</shape>
```

`shape` 로 drawable을 생성하고 `retangle` 속성을 넣어줬다. `stroke` 속성으로 회색 테두리를 지정해줬으며 `corner` 속성으로 꼭지점에 round를 넣어줬다. 새로운 `View` 가 추가됐으므로 View에다가 값들을 지정해줘야한다.  위에서 설명한 `Adapter` 를 수정하는 것으로 만족한다.

```kotlin
	inner class BookItemViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bookModel: Book) {
            binding.titleTextView.text = bookModel.title
            binding.descriptionTextView.text = bookModel.description

            binding.root.setOnClickListener{
                itemClickedListener(bookModel)
            }

            Glide
                .with(binding.coverImageView.context)
                .load(bookModel.coverSmallUrl)
                .into(binding.coverImageView)
        }
    }
```

`bind` 메소드에 title과 description을 연결해준다. 이미지의 경우 `Glide` 메소드를 활용한다. `with` 으로 `ImageView` 와 연결해주고 불러올 이미지의 url을 `load` 한다. 최종적으로 이미지를 삽입하면 끝!

## 4. 검색 창 구현하기

검색 창 역시 `Recycler View` 로 구현하려고 한다. 따라서 `Adapter` 와 `ViewHolder` 가 필요하다. 이 부분은 `BookAdapter` 클래스를 그대로 가져온 후 수정했다.

```kotlin
package com.example.bookstore.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.databinding.ItemHistoryBinding
import com.example.bookstore.model.History

class HistoryAdapter(val historyDeleteClickedListener: (String) -> Unit) :
    ListAdapter<History, HistoryAdapter.HistoryItemViewHolder>(diffUtil) {
    inner class HistoryItemViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(historyModel: History) {
            binding.historyKeywordTextView.text = historyModel.keyword
            binding.historyKeywordDeleteButton.setOnClickListener {
                historyDeleteClickedListener(historyModel.keyword.orEmpty())
            }
        }
    }

    // 뷰가 없을경우 뷰를 그려주는 메소드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        return HistoryItemViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context)))
    }

    // 뷰에 데이터를 넣어주는 bind 메소드
    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<History>() {
            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem.keyword == newItem.keyword
            }

        }
    }
}
```

`BookAdapter` 와 다른 점이 있다면 생성자를 추가해줬다는 점이다.  생성자를 추가해준 이유는 이후에 구현할 `검색 기록 삭제` 를 위함이다. 검색 기록 창에서 삭제 버튼이 눌리는 경우 해당 키워드를 삭제하는 방식으로 구현할 예정이다. `Adapter` 는 다른 어뎁터와 유사하므로 이정도로 설명하겠다. 

## 5. 검색 기록 저장하기

검색 기록을 저장하기 위해서는 어떻게 할 수 있을까? 지난번에 배웠던 `getSharedPreference` 를 사용하면 될까? 물론 사용해도 된다. `key - value` 로 저장하니까 새로운 입력이 들어올 때마다 계속 추가해주면 된다. 그러면 삭제의 경우에는? 삭제할 아이템을 제외하고 다른 리스트에 넣어두고 이 리스트로 다시 설정해 주면 되는 것인가? 너무 번거롭다... 그래서 사용할 것이 `Room` . 우선 공식 문서를 살펴보자.

![](https://images.velog.io/images/k906506/post/cf868d8f-b057-4745-9687-c763bef9972d/image.png)

[안드로이드 공식 문서 - Room](https://developer.android.com/training/data-storage/room?hl=ko)

쉽게 말해서 데이터를 효율적으로 처리할 수 있는 내장 sqlite이다. 나도 아직 공부 중이여서 정확한 설명은 못하겠다. 잘 정리된 글이 있으니 참고.

[안드로이드 Room의 사용법과 예제](https://todaycode.tistory.com/39)

아무튼 room을 사용하기 위해서는 dao라는 객체가 필요한데 dao는 `Data Access Object` 이다. 쉽게 말해 데이터에 접근할 수 있게끔 해주는 인터페이스이다. 데이터베이스에 접근하기 위해서는 뭐가 필요할까? 바로 쿼리문이다. `Insert` `Delete` 등등... 쿼리문을 통해 데이터베이스에 접근할 수 있는데 이 쿼리문을 정의해 둔 것이 바로 `Dao` 이다. 우선 `Room` 과 `Dao` 를 사용하기 위해서는 `gradle` 에 추가해줘야 한다.

```kotlin
id 'kotlin-kapt'		

implementation 'androidx.room:room-runtime:2.2.6'
kapt 'androidx.room:room-compiler:2.2.6'
```

아래 코드를 보자.

```kotlin
package com.example.bookstore.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookstore.model.History

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history")
    fun getAll() : List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history WHERE keyword == :keyword")
    fun delete(keyword:String)
}.
```

어노테이션으로 쿼리문을 선언하고 아래에 메소드를 적으면 해당 쿼리문을 실행하는 방식이다. 데이터베이스에 접근할 수 있는 쿼리문을 작성했으니 이제 데이터를 저장할 데이터베이스를 정의해야한다. 이것이 바로 `Room` 

```kotlin
package com.example.bookstore

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bookstore.dao.HistoryDao
import com.example.bookstore.dao.ReviewDao
import com.example.bookstore.model.History
import com.example.bookstore.model.Review

@Database(entities = [History::class, Review::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
}

fun getAppDatabase(context: Context): AppDatabase {
    val migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE `REVIEW`  (`id` INTEGER, `review` TEXT," + "PRIMARY KEY(`id`))")
        }
    }
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "BookSearchDB"
    )
        .addMigrations(migration_1_2)
        .build()
}
```

하단의 마이그레이션은 데이터베이스 충돌을 막기 위한 코드이다. 본 코드로 작성하고 바로 실행하면 괜찮지만 나의 경우 검색 기록 DB를 작성하고 빌드를 진행하고 다시 도서 한 권에 대한 DB를 작성해서 두 DB가 충돌을 일으켰다. 따라서 기존에 DB가 이미 존재하는 경우 새로운 DB로 덮도록 마이그레이션을 작성했다. 아무튼 DB와 쿼리문을 작성했으니 이제 `MainActivity` 에서 연결해주면 끝난다.

```kotlin
	private lateinit var db: AppDatabase	

	private fun saveSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }

    private fun deleteSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().delete(keyword)
            showHistoryView()
        }.start()
    }
```

db에 접근할 수 있는 변수를 선언하고 메소드를 호출하면 된다. 이제 마지막으로 도서 한 권에 대한 세부 페이지를 구현하면 된다.

## 6. 도서 세부 페이지 구현

세부 페이지에 보여줄 항목은 도서 사진, 도서 이름, 도서 설명이다. 그리고 도서에 대한 리뷰를 추가할 수 있도록 간단한 `EditText` 도 추가하려고 한다.

```kotlin
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ScrollView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent">

        <androidx.constraintlayout.widget.ConstraintLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <TextView
                android:id="@+id/titleTextView"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_margin="16dp"
                android:gravity="center"
                android:textColor="@color/black"
                android:textSize="24sp"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent" />

            <ImageView
                android:id="@+id/coverImageView"
                android:layout_width="300dp"
                android:layout_height="300dp"
                android:layout_marginTop="16dp"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@+id/titleTextView" />

            <TextView
                android:id="@+id/descriptionTextView"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_margin="16sp"
                android:textColor="@color/black"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@+id/coverImageView" />

            <EditText
                android:id="@+id/reviewEditText"
                android:layout_width="0dp"
                android:layout_height="300dp"
                app:layout_constraintBottom_toTopOf="@+id/saveButton"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@id/descriptionTextView" />

            <Button
                android:id="@+id/saveButton"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginHorizontal="10sp"
                android:text="@string/save_review"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent" />
        </androidx.constraintlayout.widget.ConstraintLayout>

    </ScrollView>

</androidx.constraintlayout.widget.ConstraintLayout>
```

자세한 설명은 생략하고 레이아웃과 연결한 `activity_detail` 을 살펴보자.

```kotlin
package com.example.bookstore

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.bookstore.databinding.ActivityDetailBinding
import com.example.bookstore.model.Book
import com.example.bookstore.model.Review

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = getAppDatabase(this)

        val model = intent.getParcelableExtra<Book>("BookModel")

        binding.titleTextView.text = model?.title.orEmpty()
        binding.descriptionTextView.text = model?.description.orEmpty()

        Glide.with(binding.coverImageView.context)
            .load(model?.coverSmallUrl.orEmpty())
            .into(binding.coverImageView)

        Thread {
            val review = db.reviewDao().getOneReview(model?.id?.toInt() ?: 0)
            runOnUiThread {
                binding.reviewEditText.setText(review?.review.orEmpty())
            }
        }.start()

        binding.saveButton.setOnClickListener {
            Thread {
                db.reviewDao().saveReview(
                    Review(
                        model?.id?.toInt() ?: 0, binding.reviewEditText.text.toString()
                    )
                )
            }.start()
        }
    }
}
```

역시 `activity_main` 과 유사하다. db에 기록된 리뷰에 접근해서 보여주는 것이 추가됐을 뿐? `model` 객체에 저장된 제목과 설명을 보여주고 `Glide` 로 이미지를 보여주고 저장 버튼을 클릭하면 db에 리뷰를 저장할 수 있게끔 구현했다.

# 느낀점

지금까지 만든 앱 중에서 가장 배운 것이 많은 앱이었다. `Retrofit` 으로 API와 연결하고 `Glide` 로 이미지 보여주고, `ListAdapter` 로 `RecyclerView` 구현하고... 엄청 유익했다. 이번에 앱을 만들면서 구글링을 엄청했는데 특히 `RecyclerView` 에 많은 시간이 들었다. 많이 어려웠지만 되게 뿌듯했다.

