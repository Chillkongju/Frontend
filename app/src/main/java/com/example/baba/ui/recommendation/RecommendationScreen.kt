package com.example.baba.ui.recommendation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.R
import com.example.baba.ui.theme.*

// 더미 데이터 모델
data class RecommendationItem(
    val title: String,
    val description: String,
    val imageRes: Int,
    val category: String
)

@Composable
fun RecommendationScreen() {
    // 더미 데이터
    val movieItems = listOf(
        RecommendationItem(
            "헤어질 결심",
            "산 정상에서 추락한 한 남자의 변사 사건. 담당 형사 '해준'(박해일)은 사망자의 아내 '서래'(탕웨이)와 마주하게 된다. \"산에 가서 안 오면 걱정했어요, 마침내 죽을까 봐.\" 남편의 죽음 앞에서 특별한 동요를 보이지 않는 '서래'. 경찰은 보통의 유가족과는 다른 '서래'를 용의선상에 올린다. '해준'은 사건 당일의 알리바이 탐문과 신문, 잠복수사를 통해 '서래'를 알아가면서 그녀에 대한 관심이 점점 커져가는 것을 느낀다. 한편, 좀처럼 속을 짐작하기 어려운 '서래'는 상대가 자신을 의심한다는 것을 알면서도 조금의 망설임도 없이 '해준'을 대하는데…. 진심을 숨기는 용의자 용의자에게 의심과 관심을 동시에 느끼는 형사 그들의 <헤어질 결심>",
            R.drawable.movie_poster_1,
            "영화"
        ),
        RecommendationItem(
            "이터널 선샤인",
            "조엘은 아픈 기억만을 지워준다는 라쿠나사를 찾아가 헤어진 연인 클레멘타인의 기억을 지우기로 결심한다. 기억이 사라져 갈수록 조엘은 사랑이 시작되던 순간, 행복한 기억들, 가슴 속에 각인된 추억들을 지우기 싫어지기만 하는데... 당신을 지우면 이 아픔도 사라질까요? 사랑은 그렇게 다시 기억된다.",
            R.drawable.movie_poster_2,
            "영화"
        )
    )

    val bookItems = listOf(
        RecommendationItem(
            "희망",
            "쓸쓸한 시대를 통과하는 우리들의 주문, 잘가라 밤이여\n" +
                    "상처와 절망으로 얼룩진 〈나성여관〉에서 희망을 말하다\n" +
                    "시간이 흘러도 변하지 않는 양귀자 소설의 재미와 감동\n" +
                    "\n" +
                    "작가 양귀자가 1990년 발표한 첫 장편소설. 1986년, 연작소설 『원미동 사람들』로 80년대 한국 사회의 척박한 시대 지형을 놀랍도록 세밀하게 그려내 주목을 받았던 작가가 처음으로 펴낸 장편소설이다. 90년 초판의 제목은 『잘가라 밤이여』였으나 다음 해 『희망』으로 제목을 바꾸어 재출간했다. “잘가라 밤이여”의 은유에서 벗어나 명료하게 “희망”으로 가고 싶다는 작가의 뜻을 반영했다.\n" +
                    "\n" +
                    "이 소설은 특히 작가 고유의 연민과 따스한 시선이, 그리고 양귀자 특유의 활달하고 서슴없는 문체가 휘몰아치는 시대의 거칠고 황량한 삽화들을 어떻게 이야기로 보듬어 완성하는지를 여실히 보여주고 있어 양귀자의 문학을 이해하는데 중요한 지표가 된다.",
            R.drawable.book_poster_1,
            "도서"
        ),
        RecommendationItem(
            "급류",
            "“너 소용돌이에 빠지면 어떻게 해야 하는 줄 알아?\n" +
                    "수면에서 나오려 하지 말고 숨 참고 밑바닥까지 잠수해서 빠져나와야 돼.”\n" +
                    "\n" +
                    "상처에 흠뻑 젖은 이들이 각자의 몸을 말리기까지,\n" +
                    "서로의 흉터를 감싸며 다시 무지개를 보기까지\n" +
                    "거센 물살 같은 시간 속에서 헤엄치는 법을 알아내는\n" +
                    "연약한 이들의 용감한 성장담, 단 하나의 사랑론\n" +
                    "\n" +
                    "2020년 《한경신춘문예》에 장편소설 『GV 빌런 고태경』이 당선되어 작품 활동을 시작한 소설가 정대건의 두 번째 장편소설 『급류』가 오늘의 젊은 작가 시리즈 40번으로 출간되었다. 『급류』는 저수지와 계곡이 유명한 지방도시 ‘진평’을 배경으로, 열일곱 살 동갑내기인 ‘도담’과 ‘해솔’의 만남과 사랑을 그린 소설이다. 아빠와 함께 수영을 하러 갔던 도담이 한눈에 인상적인 남자아이 ‘해솔’이 물에 빠질 뻔한 것을 구하러 뛰어들며 둘의 인연은 시작된다. 운명적이고 낭만적으로 보이는 첫 만남 이후 둘은 모든 걸 이야기하고 비밀 없는 사이가 되지만, 그 첫사랑이 잔잔한 물처럼 평탄하지만은 않다. 모르는 사이에 디뎌 빠져나올 수 없이 빨려드는 와류처럼 둘의 관계는 우연한 사건으로 다른 국면을 맞이한다. 도담과 해솔의 관계가 연인으로 발전하던 어느 날, 해솔의 엄마와 도담의 아빠가 불륜 관계인 듯한 정황이 드러나고 이에 화가 난 도담은 그 둘이 은밀히 만나기로 한 날 밤 랜턴을 들고 그들의 뒤를 밟는다. 그리고 그곳에서 생각지도 못한 사고가 벌어진다. 그날 이후, 진평에서 오직 서로가 전부이던, 나누지 못할 비밀이 없던 도담과 해솔의 관계와 삶은 순식간에 바뀌어 버린다. 해솔의 엄마와 도담의 아빠는 어떤 관계였던 걸까? 그 날, 그 밤 도담과 해솔은 어떤 일을 겪게 된 걸까?",
            R.drawable.book_poster_2,
            "도서"
        )
    )

    val concertItems = listOf(
        RecommendationItem(
            "레드북",
            "\"나는 나를 말하는 사람\" 신사의 나라 영국, 그중에서도 가장 보수적인었던 빅토리아 시대 약혼자에게 첫 경험을 고백했다가 파혼당하고 도시로 건너온 여인 '안나'는 힘들고 외로울 때마다 첫사랑과의 추억을 떠올리며 하루하루 굳세게 살아간다. 어느 날, 신사 중의 신사인 '브라운'이란 청년이 '안나'를 찾아온다. '안나'는 '브라운'의 의도를 알 수 없는 수상한 응원에 받으며 여성들만의 고품격 문학회 로렐라이 언덕에 들어가 자신의 추억을 소설로 쓴다. '안나'의 소설이 실린 잡지 레드북은 여성이 자신의 신체를 언급하는 것조차 금기시되던 사회 분위기로 인해 거센 비난을 받는다.",
            R.drawable.show_poster_1,
            "공연"
        ),
        RecommendationItem(
            "여신님이 보고계셔",
            "한국 전쟁이 한창이던 당시, 국군 대위 한영범은 인민군 이창섭, 류순호, 변주화, 조동현을 포로수용소로 이송하는 특별 임무를 부여받고 부하 신석구와 함께 이송선에 오른다. 그러나 포로들은 배 위에서 폭동을 일으키고, 폭동 중에 기상악화로 고장 나버린 이송선 때문에 여섯 명의 병사들은 무인도에 고립된다. 유일하게 배를 수리할 수 있는 순호는 전쟁 후유증으로 정신을 놓은 상태. 생존 본능만 남겨진 채 병사들은 점점 야만적으로 변해간다. 그 와중에 인질이 된 영범은 악몽에 시달리는 순호에게 여신 이야기를 만들어 들려주고, 순호는 여신님에 빠져 안정을 되찾아 간다. 모두는 순호를 변화시키기 위해 ‘여신님이 보고 계셔 대작전’을 시작하고 가상의 여신님을 위해 공동의 규칙을 세우는데… 살아남기 위해 그들이 만든 신비의 여신, 과연 그들은 여신님과 함께 무사히 살아갈 수 있을까?",
            R.drawable.show_poster_2,
            "공연"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 전체 배경을 White로
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White) // 헤더 배경 White
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = "문화생활 추천",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CoolGray100)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(1.dp))
            }

            item {
                RecommendationSection(
                    categoryTitle = "영화",
                    categoryIconRes = R.drawable.recommend_movie,
                    items = movieItems
                )
            }

            item {
                RecommendationSection(
                    categoryTitle = "도서",
                    categoryIconRes = R.drawable.recommend_book,
                    items = bookItems
                )
            }

            item {
                RecommendationSection(
                    categoryTitle = "공연",
                    categoryIconRes = R.drawable.recommend_show,
                    items = concertItems
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RecommendationSection(
    categoryTitle: String,
    categoryIconRes: Int,
    items: List<RecommendationItem>
) {
    Column {
        // 카테고리 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Image(
                painter = painterResource(id = categoryIconRes),
                contentDescription = categoryTitle,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = categoryTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Blue2
            )
        }

        items.take(2).forEach { item ->
            RecommendationCard(
                item = item,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun RecommendationCard(
    item: RecommendationItem,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isExpanded) {
                    Modifier.wrapContentHeight()
                } else {
                    Modifier.height(172.dp)
                }
            )
            .clip(RoundedCornerShape(12.dp))
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                modifier = Modifier
                    .width(100.dp)
                    .height(143.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextBlack
                )

                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    color = CoolGray700,
                    lineHeight = 18.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
                )

                Text(
                    text = if (isExpanded) "접기" else "더보기",
                    fontSize = 12.sp,
                    color = Blue3,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        isExpanded = !isExpanded
                    }
                )
            }
        }
    }
}