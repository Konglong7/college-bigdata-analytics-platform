package com.univ.bigdata.service;

import com.univ.bigdata.dto.RecommendQueryDto;
import com.univ.bigdata.vo.RecommendResultVo;
import com.univ.bigdata.vo.RecommendSchoolVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RecommendServiceTest {

    @Autowired
    private RecommendService recommendService;

    @Test
    public void testRecommendHunan550() {
        RecommendQueryDto query = new RecommendQueryDto();
        query.setProvince("湖南");
        query.setScore(550.0);
        query.setSubjectType("物理类");
        query.setTargetLevel("全部");
        query.setTargetProvince("湖南");

        RecommendResultVo result = recommendService.recommendVolunteers(query);
        assertNotNull(result);
        System.out.println("=== 550分 湖南物理类推荐结果 ===");
        System.out.println("冲刺高校数量: " + result.getRushList().size());
        System.out.println("稳妥高校数量: " + result.getSteadyList().size());
        System.out.println("保底高校数量: " + result.getSafeList().size());

        // 验证三列均有推荐
        assertFalse(result.getRushList().isEmpty(), "冲刺高校不应为空");
        assertFalse(result.getSteadyList().isEmpty(), "稳妥高校不应为空");
        assertFalse(result.getSafeList().isEmpty(), "保底高校不应为空");

        // 验证分数多样性（绝不能所有学校都是 508.5 分）
        Set<Double> scores = new HashSet<>();
        for (RecommendSchoolVo s : result.getSafeList()) {
            scores.add(s.getPredictScore());
            System.out.println("保底高校: " + s.getSchoolName() + ", 预测线: " + s.getPredictScore() + ", 专业: " + s.getTopMajors());
        }
        for (RecommendSchoolVo s : result.getSteadyList()) {
            scores.add(s.getPredictScore());
            System.out.println("稳妥高校: " + s.getSchoolName() + ", 预测线: " + s.getPredictScore() + ", 专业: " + s.getTopMajors());
        }
        for (RecommendSchoolVo s : result.getRushList()) {
            scores.add(s.getPredictScore());
            System.out.println("冲刺高校: " + s.getSchoolName() + ", 预测线: " + s.getPredictScore() + ", 专业: " + s.getTopMajors());
        }

        assertTrue(scores.size() > 3, "推荐高校的分数线必须具有连续多样性，不能全部相同");
    }
}
