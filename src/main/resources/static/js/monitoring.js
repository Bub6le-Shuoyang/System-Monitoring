// 全局变量
let stompClient = null;
let dataStreamActive = true;
let currentTheme = 'dark';
let timeSeriesChart = null;
let distributionChart = null;
let historyPlayback = false;

// 页面加载完成后初始化
$(document).ready(function() {
    console.log('页面DOM加载完成，开始初始化...');
    
    // 按顺序初始化，确保依赖关系正确
    initializeEventHandlers();
    
    // 初始化图表并等待成功
    console.log('开始初始化图表...');
    const chartInitResult = initializeCharts();
    
    // 如果Chart.js未加载，等待手动加载完成
    if (chartInitResult === false && typeof Chart === 'undefined') {
        console.log('Chart.js正在手动加载中，等待加载完成...');
        // 设置定时检查Chart.js是否加载完成
        const checkChartInterval = setInterval(function() {
            if (typeof Chart !== 'undefined') {
                console.log('Chart.js手动加载完成，重新初始化图表...');
                clearInterval(checkChartInterval);
                setTimeout(initializeCharts, 500);
            }
        }, 500);
        
        // 10秒后超时，不再等待
        setTimeout(function() {
            clearInterval(checkChartInterval);
            if (typeof Chart === 'undefined') {
                console.error('Chart.js加载超时，继续初始化其他组件...');
                // 即使图表初始化失败，也尝试初始化WebSocket
                setTimeout(function() {
                    console.log('初始化WebSocket连接...');
                    initializeWebSocket();
                }, 1500);
            }
        }, 10000);
        
        return;
    }
    
    if (chartInitResult) {
        console.log('图表初始化成功，开始加载数据...');
        // 图表初始化完成后再加载数据
        setTimeout(function() {
            console.log('开始加载初始数据...');
            loadInitialData();
            
            // 数据加载完成后再初始化WebSocket
            setTimeout(function() {
                console.log('初始化WebSocket连接...');
                initializeWebSocket();
                
                // 最后启动数据生成
                setTimeout(function() {
                    console.log('启动数据生成...');
                    startDataGeneration();
                }, 1000);
            }, 1000);
        }, 500);
    } else {
        console.error('图表初始化失败，跳过数据加载');
        // 即使图表初始化失败，也尝试初始化WebSocket
        setTimeout(function() {
            console.log('初始化WebSocket连接...');
            initializeWebSocket();
        }, 1500);
    }
});

// 初始化WebSocket连接
function initializeWebSocket() {
    try {
        console.log('开始初始化WebSocket连接...');
        
        // 检查SockJS和Stomp是否已加载
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.error('SockJS或Stomp未加载');
            showNotification('WebSocket库未加载，实时功能不可用', 'warning');
            return;
        }
        
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        
        // 设置调试模式
        stompClient.debug = function(str) {
            console.log('STOMP Debug: ' + str);
        };
        
        stompClient.connect({}, function(frame) {
            console.log('WebSocket连接成功: ' + frame);
            showNotification('WebSocket连接成功', 'success');
            
            // 订阅系统指标数据
            stompClient.subscribe('/topic/metrics', function(message) {
                if (dataStreamActive && !historyPlayback) {
                    const metrics = JSON.parse(message.body);
                    console.log('收到系统指标数据:', metrics);
                    updateMetricsDisplay(metrics);
                    updateCharts(metrics);
                }
            });
            
            // 订阅系统健康状态
            stompClient.subscribe('/topic/health', function(message) {
                if (dataStreamActive && !historyPlayback) {
                    const health = JSON.parse(message.body);
                    console.log('收到系统健康状态:', health);
                    updateHealthStatus(health);
                }
            });
            
            // 订阅任务数据
            stompClient.subscribe('/topic/tasks', function(message) {
                if (dataStreamActive && !historyPlayback) {
                    const tasks = JSON.parse(message.body);
                    console.log('收到任务数据:', tasks);
                    updateTaskList(tasks);
                }
            });
            
            // 订阅任务摘要
            stompClient.subscribe('/topic/task-summary', function(message) {
                if (dataStreamActive && !historyPlayback) {
                    const summary = JSON.parse(message.body);
                    console.log('收到任务摘要:', summary);
                    updateTaskSummary(summary);
                }
            });
            
            // 订阅告警数据
            stompClient.subscribe('/topic/alerts', function(message) {
                if (dataStreamActive && !historyPlayback) {
                    const alerts = JSON.parse(message.body);
                    console.log('收到告警数据:', alerts);
                    updateAlertList(alerts);
                }
            });
            
            // 订阅告警摘要
            stompClient.subscribe('/topic/alert-summary', function(message) {
                if (dataStreamActive && !historyPlayback) {
                    const summary = JSON.parse(message.body);
                    console.log('收到告警摘要:', summary);
                    updateAlertSummary(summary);
                }
            });
            
            // 订阅新数据
            stompClient.subscribe('/topic/new-metric', function(message) {
                if (dataStreamActive && !historyPlayback) {
                    const metric = JSON.parse(message.body);
                    console.log('收到新指标:', metric);
                    addNewMetric(metric);
                }
            });
            
            stompClient.subscribe('/topic/new-task', function(message) {
                if (dataStreamActive && !historyPlayback) {
                    const task = JSON.parse(message.body);
                    console.log('收到新任务:', task);
                    addNewTask(task);
                }
            });
            
            stompClient.subscribe('/topic/new-alert', function(message) {
                if (dataStreamActive && !historyPlayback) {
                    const alert = JSON.parse(message.body);
                    console.log('收到新告警:', alert);
                    addNewAlert(alert);
                }
            });
        }, function(error) {
            console.error('WebSocket连接错误:', error);
            showNotification('WebSocket连接失败: ' + error, 'error');
        });
    } catch (error) {
        console.error('初始化WebSocket时发生错误:', error);
        showNotification('WebSocket初始化失败', 'error');
    }
}

// 初始化事件处理器
function initializeEventHandlers() {
    // 数据流控制
    $('#toggleDataStream').click(function() {
        dataStreamActive = !dataStreamActive;
        const btn = $(this);
        if (dataStreamActive) {
            btn.html('<i class="bi bi-pause-fill"></i> 暂停数据流');
            btn.removeClass('btn-warning').addClass('btn-primary');
        } else {
            btn.html('<i class="bi bi-play-fill"></i> 启动数据流');
            btn.removeClass('btn-primary').addClass('btn-warning');
        }
    });
    
    // 刷新数据
    $('#refreshData').click(function() {
        loadInitialData();
        showNotification('数据已刷新', 'success');
    });
    
    // 主题切换
    $('#toggleTheme').click(function() {
        toggleTheme();
    });
    
    // 搜索功能
    $('#searchInput').on('input', function() {
        const searchTerm = $(this).val().toLowerCase();
        filterData(searchTerm);
    });
    
    // 维度切换
    $('#dimensionSelect').change(function() {
        const dimension = $(this).val();
        updateChartsDimension(dimension);
    });
    
    // 时间范围切换
    $('#timeRange').change(function() {
        const timeRange = $(this).val();
        loadHistoricalData(timeRange);
    });
    
    // 历史回放
    $('#playHistory').click(function() {
        toggleHistoryPlayback();
    });
}

// 初始化图表
function initializeCharts() {
    try {
        console.log('开始初始化图表...');
        
        // 等待DOM完全加载
        if (document.readyState !== 'complete') {
            console.log('DOM未完全加载，等待...');
            setTimeout(initializeCharts, 100);
            return;
        }
        
        // 确保canvas元素存在
        const timeSeriesCanvas = document.getElementById('timeSeriesChart');
        const distributionCanvas = document.getElementById('distributionChart');
        
        if (!timeSeriesCanvas) {
            console.error('时序图表canvas元素未找到');
            return;
        }
        
        if (!distributionCanvas) {
            console.error('分布图表canvas元素未找到');
            return;
        }
        
        // 确保Chart.js已加载
        if (typeof Chart === 'undefined') {
            console.error('Chart.js未加载，尝试手动加载...');
            
            // 优先尝试加载CDN版本
            const script = document.createElement('script');
            script.src = 'https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js';
            script.onload = function() {
                console.log('CDN加载Chart.js成功，重新初始化图表...');
                setTimeout(initializeCharts, 500);
            };
            script.onerror = function() {
                console.error('CDN加载Chart.js失败，尝试本地webjars...');
                // 如果CDN失败，尝试本地webjars
                const localScript = document.createElement('script');
                localScript.src = '/webjars/npm/chart.js/3.9.1/dist/chart.min.js';
                localScript.onload = function() {
                    console.log('本地webjars加载Chart.js成功，重新初始化图表...');
                    setTimeout(initializeCharts, 500);
                };
                localScript.onerror = function() {
                    console.error('本地webjars加载Chart.js也失败');
                };
                document.head.appendChild(localScript);
            };
            document.head.appendChild(script);
            
            return false;
        }
        
        console.log('Canvas元素和Chart.js都已就绪，开始创建图表...');
        
        // 时序图表
        const timeSeriesCtx = timeSeriesCanvas.getContext('2d');
        timeSeriesChart = new Chart(timeSeriesCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'CPU使用率',
                    data: [],
                    borderColor: '#00d4ff',
                    backgroundColor: 'rgba(0, 212, 255, 0.1)',
                    tension: 0.4,
                    fill: true
                }, {
                    label: '内存使用率',
                    data: [],
                    borderColor: '#00ff88',
                    backgroundColor: 'rgba(0, 255, 136, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: {
                            color: '#e6eef9'
                        }
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                scales: {
                    x: {
                        ticks: {
                            color: '#9aa5b1'
                        },
                        grid: {
                            color: '#2a2f55'
                        }
                    },
                    y: {
                        beginAtZero: true,
                        max: 100,
                        ticks: {
                            color: '#9aa5b1',
                            callback: function(value) {
                                return value + '%';
                            }
                        },
                        grid: {
                            color: '#2a2f55'
                        }
                    }
                },
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                }
            }
        });
        
        // 分布图表
        const distributionCtx = distributionCanvas.getContext('2d');
        distributionChart = new Chart(distributionCtx, {
            type: 'doughnut',
            data: {
                labels: [],
                datasets: [{
                    data: [],
                    backgroundColor: [
                        '#00d4ff',
                        '#00ff88',
                        '#ffb800',
                        '#ff4757',
                        '#a55eea',
                        '#ff6b6b',
                        '#4ecdc4',
                        '#45b7d1'
                    ],
                    borderWidth: 2,
                    borderColor: '#1a1d3e'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                        labels: {
                            color: '#e6eef9',
                            padding: 15,
                            font: {
                                size: 12
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.raw || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                                return `${label}: ${percentage}%`;
                            }
                        }
                    }
                },
                cutout: '60%'
            }
        });
        
        console.log('图表初始化成功');
        return true; // 返回成功标志
    } catch (error) {
        console.error('图表初始化失败:', error);
        return false; // 返回失败标志
    }
}

// 加载初始数据
function loadInitialData() {
    console.log('开始加载初始数据...');
    
    // 加载系统指标
    $.ajax({
        url: '/api/metrics/recent?minutes=5',
        method: 'GET',
        success: function(data) {
            console.log('系统指标数据加载成功:', data);
            updateMetricsDisplay(data);
            updateCharts(data);
        },
        error: function(xhr, status, error) {
            console.error('加载系统指标数据失败:', error);
            showNotification('加载系统指标数据失败', 'error');
        }
    });
    
    // 加载任务数据
    $.ajax({
        url: '/api/tasks',
        method: 'GET',
        success: function(data) {
            console.log('任务数据加载成功:', data);
            updateTaskList(data);
        },
        error: function(xhr, status, error) {
            console.error('加载任务数据失败:', error);
            showNotification('加载任务数据失败', 'error');
        }
    });
    
    $.ajax({
        url: '/api/tasks/summary',
        method: 'GET',
        success: function(data) {
            console.log('任务摘要数据加载成功:', data);
            updateTaskSummary(data);
        },
        error: function(xhr, status, error) {
            console.error('加载任务摘要数据失败:', error);
            showNotification('加载任务摘要数据失败', 'error');
        }
    });
    
    // 加载告警数据
    $.ajax({
        url: '/api/alerts/unresolved',
        method: 'GET',
        success: function(data) {
            console.log('告警数据加载成功:', data);
            updateAlertList(data);
        },
        error: function(xhr, status, error) {
            console.error('加载告警数据失败:', error);
            showNotification('加载告警数据失败', 'error');
        }
    });
    
    $.ajax({
        url: '/api/alerts/summary',
        method: 'GET',
        success: function(data) {
            console.log('告警摘要数据加载成功:', data);
            updateAlertSummary(data);
        },
        error: function(xhr, status, error) {
            console.error('加载告警摘要数据失败:', error);
            showNotification('加载告警摘要数据失败', 'error');
        }
    });
    
    // 加载系统健康状态
    $.ajax({
        url: '/api/metrics/health',
        method: 'GET',
        success: function(data) {
            console.log('系统健康状态数据加载成功:', data);
            updateHealthStatus(data);
        },
        error: function(xhr, status, error) {
            console.error('加载系统健康状态数据失败:', error);
            showNotification('加载系统健康状态数据失败', 'error');
        }
    });
}

// 更新系统指标显示
function updateMetricsDisplay(metrics) {
    if (!metrics || metrics.length === 0) return;
    
    // 计算平均值
    const avgCpu = metrics.reduce((sum, m) => sum + m.cpuUsage, 0) / metrics.length;
    const avgMemory = metrics.reduce((sum, m) => sum + m.memoryUsage, 0) / metrics.length;
    const avgDisk = metrics.reduce((sum, m) => sum + m.diskUsage, 0) / metrics.length;
    const avgLoad = metrics.reduce((sum, m) => sum + m.loadAverage, 0) / metrics.length;
    const avgNetworkIn = metrics.reduce((sum, m) => sum + m.networkIn, 0) / metrics.length;
    const avgNetworkOut = metrics.reduce((sum, m) => sum + m.networkOut, 0) / metrics.length;
    
    // 更新显示
    $('#cpuUsage').text(avgCpu.toFixed(1) + '%');
    $('#memoryUsage').text(avgMemory.toFixed(1) + '%');
    $('#diskUsage').text(avgDisk.toFixed(1) + '%');
    $('#loadAverage').text(avgLoad.toFixed(2));
    $('#networkIn').text(avgNetworkIn.toFixed(0) + ' MB/s');
    $('#networkOut').text(avgNetworkOut.toFixed(0) + ' MB/s');
    
    // 更新进度条
    $('#cpuBar').css('width', avgCpu + '%');
    $('#memoryBar').css('width', avgMemory + '%');
    $('#diskBar').css('width', avgDisk + '%');
    $('#loadBar').css('width', Math.min(avgLoad * 12.5, 100) + '%');
    
    // 更新数据表格
    updateMetricsTable(metrics);
}

// 更新健康状态
function updateHealthStatus(health) {
    const healthLight = $('#healthStatus');
    const loadBalanceText = $('#loadBalanceText');
    
    healthLight.removeClass('healthy warning unhealthy');
    
    if (health.status === '健康') {
        healthLight.addClass('healthy');
        loadBalanceText.text('负载均衡状态: 正常');
    } else if (health.status === '警告') {
        healthLight.addClass('warning');
        loadBalanceText.text('负载均衡状态: 轻微倾斜');
    } else {
        healthLight.addClass('unhealthy');
        loadBalanceText.text('负载均衡状态: 严重倾斜');
    }
}

// 更新任务列表
function updateTaskList(tasks) {
    const taskList = $('#taskList');
    taskList.empty();
    
    tasks.slice(0, 10).forEach(function(task) {
        const taskItem = createTaskItem(task);
        taskList.append(taskItem);
    });
}

// 创建任务项
function createTaskItem(task) {
    const statusClass = task.status.toLowerCase();
    const statusText = getStatusText(task.status);
    
    return $(`
        <div class="task-item ${statusClass} fade-in" data-task-id="${task.id}">
            <div class="task-name">${task.taskName}</div>
            <div class="task-cluster">目标集群: ${task.targetCluster}</div>
            <div class="task-progress">
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${task.progress}%"></div>
                </div>
                <div class="task-status">${statusText}</div>
            </div>
        </div>
    `).click(function() {
        showTaskDetail(task);
    });
}

// 更新任务摘要
function updateTaskSummary(summary) {
    $('#queuedCount').text(summary.queuedCount);
    $('#runningCount').text(summary.runningCount);
    $('#failedCount').text(summary.failedCount);
    $('#completedCount').text(summary.completedCount);
}

// 更新告警列表
function updateAlertList(alerts) {
    const alertList = $('#alertList');
    alertList.empty();
    
    alerts.slice(0, 5).forEach(function(alert) {
        const alertItem = createAlertItem(alert);
        alertList.append(alertItem);
    });
}

// 创建告警项
function createAlertItem(alert) {
    const severityClass = alert.severity.toLowerCase();
    const severityText = getSeverityText(alert.severity);
    const timeAgo = getTimeAgo(alert.timestamp);
    
    return $(`
        <div class="alert-item ${severityClass} fade-in">
            <div class="alert-source">${alert.source}</div>
            <div class="alert-message">${alert.message}</div>
            <div class="alert-time">${severityText} • ${timeAgo}</div>
        </div>
    `);
}

// 更新告警摘要
function updateAlertSummary(summary) {
    // 可以在这里添加告警摘要的显示逻辑
}

// 更新数据表格
function updateMetricsTable(metrics) {
    const tableBody = $('#metricsTableBody');
    tableBody.empty();
    
    metrics.slice(0, 10).forEach(function(metric) {
        const row = $(`
            <tr>
                <td>${metric.serverName}</td>
                <td>${metric.region}</td>
                <td>${metric.serviceType}</td>
                <td>${metric.cpuUsage.toFixed(1)}%</td>
                <td>${metric.memoryUsage.toFixed(1)}%</td>
                <td>${metric.diskUsage.toFixed(1)}%</td>
                <td>${metric.networkIn.toFixed(0)} MB/s</td>
                <td>${metric.networkOut.toFixed(0)} MB/s</td>
                <td>${metric.loadAverage.toFixed(2)}</td>
                <td>${formatTime(metric.timestamp)}</td>
            </tr>
        `);
        tableBody.append(row);
    });
}

// 更新图表
function updateCharts(metrics) {
    try {
        console.log('开始更新图表，数据:', metrics);
        
        if (!metrics || metrics.length === 0) {
            console.warn('没有指标数据可用于更新图表');
            return;
        }
        
        // 确保图表已初始化
        if (!timeSeriesChart || !distributionChart) {
            console.error('图表未初始化，尝试重新初始化...');
            // 尝试重新初始化图表
            if (initializeCharts()) {
                console.log('图表重新初始化成功，继续更新数据...');
                // 重新初始化成功后，再次调用updateCharts
                setTimeout(() => updateCharts(metrics), 500);
            }
            return;
        }
        
        // 按时间戳排序
        const sortedMetrics = metrics.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
        
        // 更新时序图表
        const labels = sortedMetrics.map(m => formatTime(m.timestamp));
        const cpuData = sortedMetrics.map(m => m.cpuUsage);
        const memoryData = sortedMetrics.map(m => m.memoryUsage);
        
        console.log('时序图表数据 - 标签:', labels, 'CPU:', cpuData, '内存:', memoryData);
        
        timeSeriesChart.data.labels = labels;
        timeSeriesChart.data.datasets[0].data = cpuData;
        timeSeriesChart.data.datasets[1].data = memoryData;
        timeSeriesChart.update('none'); // 使用 'none' 模式避免动画，提高性能
        
        // 更新分布图表
        const dimension = $('#dimensionSelect').val();
        updateDistributionChart(sortedMetrics, dimension);
        
        console.log('图表更新完成');
    } catch (error) {
        console.error('更新图表时发生错误:', error);
        showNotification('更新图表失败', 'error');
    }
}

// 更新分布图表
function updateDistributionChart(metrics, dimension) {
    try {
        console.log('开始更新分布图表，维度:', dimension);
        
        if (!distributionChart) {
            console.error('分布图表未初始化');
            return;
        }
        
        const distribution = {};
        
        metrics.forEach(metric => {
            let key;
            switch (dimension) {
                case 'server':
                    key = metric.serverName;
                    break;
                case 'region':
                    key = metric.region;
                    break;
                case 'service':
                    key = metric.serviceType;
                    break;
                default:
                    key = metric.serverName;
            }
            
            if (!distribution[key]) {
                distribution[key] = {
                    count: 0,
                    totalCpu: 0,
                    totalMemory: 0,
                    avgCpu: 0
                };
            }
            distribution[key].count++;
            distribution[key].totalCpu += metric.cpuUsage;
            distribution[key].totalMemory += metric.memoryUsage;
        });
        
        // 计算平均值
        Object.keys(distribution).forEach(key => {
            const data = distribution[key];
            data.avgCpu = data.totalCpu / data.count;
        });
        
        // 按平均值排序
        const sortedDistribution = Object.entries(distribution)
            .sort((a, b) => b[1].avgCpu - a[1].avgCpu)
            .slice(0, 8); // 限制显示前8个
        
        const labels = sortedDistribution.map(item => item[0]);
        const data = sortedDistribution.map(item => item[1].avgCpu);
        
        console.log('分布图表数据 - 标签:', labels, '数据:', data);
        
        distributionChart.data.labels = labels;
        distributionChart.data.datasets[0].data = data;
        distributionChart.update('none'); // 使用 'none' 模式避免动画，提高性能
        
        console.log('分布图表更新完成');
    } catch (error) {
        console.error('更新分布图表时发生错误:', error);
        showNotification('更新分布图表失败', 'error');
    }
}

// 显示任务详情
function showTaskDetail(task) {
    const modal = $('#taskDetailModal');
    const content = $('#taskDetailContent');
    
    content.html(`
        <p><strong>任务名称:</strong> ${task.taskName}</p>
        <p><strong>目标集群:</strong> ${task.targetCluster}</p>
        <p><strong>状态:</strong> ${getStatusText(task.status)}</p>
        <p><strong>进度:</strong> ${task.progress}%</p>
        <p><strong>创建时间:</strong> ${formatTime(task.createdTime)}</p>
        <p><strong>更新时间:</strong> ${formatTime(task.updatedTime)}</p>
    `);
    
    modal.modal('show');
}

// 生成模拟数据
function startDataGeneration() {
    console.log('开始生成模拟数据...');
    setInterval(function() {
        if (dataStreamActive && Math.random() > 0.7) {
            $.ajax({
                url: '/api/metrics/generate',
                method: 'POST',
                success: function(data) {
                    console.log('生成模拟指标数据成功:', data);
                },
                error: function(xhr, status, error) {
                    console.error('生成模拟指标数据失败:', error);
                }
            });
        }
        
        if (dataStreamActive && Math.random() > 0.8) {
            $.ajax({
                url: '/api/tasks/generate',
                method: 'POST',
                success: function(data) {
                    console.log('生成模拟任务数据成功:', data);
                },
                error: function(xhr, status, error) {
                    console.error('生成模拟任务数据失败:', error);
                }
            });
        }
        
        if (dataStreamActive && Math.random() > 0.9) {
            $.ajax({
                url: '/api/alerts/generate',
                method: 'POST',
                success: function(data) {
                    console.log('生成模拟告警数据成功:', data);
                },
                error: function(xhr, status, error) {
                    console.error('生成模拟告警数据失败:', error);
                }
            });
        }
    }, 3000);
}

// 添加新指标
function addNewMetric(metric) {
    try {
        console.log('添加新指标:', metric);
        
        // 确保图表已初始化
        if (!timeSeriesChart || !distributionChart) {
            console.error('图表未初始化，无法添加新指标');
            return;
        }
        
        // 实时添加到时序图表
        if (timeSeriesChart.data.labels.length > 20) {
            timeSeriesChart.data.labels.shift();
            timeSeriesChart.data.datasets[0].data.shift();
            timeSeriesChart.data.datasets[1].data.shift();
        }
        
        timeSeriesChart.data.labels.push(formatTime(metric.timestamp));
        timeSeriesChart.data.datasets[0].data.push(metric.cpuUsage);
        timeSeriesChart.data.datasets[1].data.push(metric.memoryUsage);
        timeSeriesChart.update('none');
        
        // 获取当前维度的最新数据并更新分布图表
        const dimension = $('#dimensionSelect').val();
        $.ajax({
            url: '/api/metrics/recent?minutes=5',
            method: 'GET',
            success: function(data) {
                updateDistributionChart(data, dimension);
            },
            error: function(xhr, status, error) {
                console.error('获取最新指标数据失败:', error);
            }
        });
        
        console.log('新指标添加完成');
    } catch (error) {
        console.error('添加新指标时发生错误:', error);
        showNotification('添加新指标失败', 'error');
    }
}

// 添加新任务
function addNewTask(task) {
    const taskList = $('#taskList');
    const taskItem = createTaskItem(task);
    taskList.prepend(taskItem);
    
    // 限制显示数量
    if (taskList.children().length > 10) {
        taskList.children().last().remove();
    }
}

// 添加新告警
function addNewAlert(alert) {
    const alertList = $('#alertList');
    const alertItem = createAlertItem(alert);
    alertList.prepend(alertItem);
    
    // 限制显示数量
    if (alertList.children().length > 5) {
        alertList.children().last().remove();
    }
}

// 切换主题
function toggleTheme() {
    // 这里可以实现主题切换逻辑
    showNotification('主题切换功能开发中', 'info');
}

// 过滤数据
function filterData(searchTerm) {
    // 实现搜索过滤逻辑
    $('.task-item, .alert-item').each(function() {
        const text = $(this).text().toLowerCase();
        $(this).toggle(text.includes(searchTerm));
    });
}

// 更新图表维度
function updateChartsDimension(dimension) {
    console.log('更新图表维度:', dimension);
    $.ajax({
        url: '/api/metrics/recent?minutes=5',
        method: 'GET',
        success: function(data) {
            console.log('维度数据加载成功:', data);
            updateDistributionChart(data, dimension);
        },
        error: function(xhr, status, error) {
            console.error('加载维度数据失败:', error);
            showNotification('加载维度数据失败', 'error');
        }
    });
}

// 加载历史数据
function loadHistoricalData(timeRange) {
    console.log('加载历史数据，时间范围:', timeRange);
    $.ajax({
        url: `/api/metrics/recent?minutes=${timeRange}`,
        method: 'GET',
        success: function(data) {
            console.log('历史数据加载成功:', data);
            updateCharts(data);
            updateMetricsDisplay(data);
        },
        error: function(xhr, status, error) {
            console.error('加载历史数据失败:', error);
            showNotification('加载历史数据失败', 'error');
        }
    });
}

// 切换历史回放
function toggleHistoryPlayback() {
    historyPlayback = !historyPlayback;
    const btn = $('#playHistory');
    
    if (historyPlayback) {
        btn.html('<i class="bi bi-pause-fill"></i> 停止回放');
        btn.removeClass('btn-outline-primary').addClass('btn-outline-danger');
        startHistoryPlayback();
    } else {
        btn.html('<i class="bi bi-play-fill"></i> 历史回放');
        btn.removeClass('btn-outline-danger').addClass('btn-outline-primary');
    }
}

// 开始历史回放
function startHistoryPlayback() {
    // 实现历史回放逻辑
    showNotification('历史回放功能开发中', 'info');
}

// 显示通知
function showNotification(message, type) {
    // 创建通知元素
    const notification = $(`
        <div class="alert alert-${type} alert-dismissible fade show position-fixed" 
             style="top: 80px; right: 20px; z-index: 9999; min-width: 300px;">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `);
    
    $('body').append(notification);
    
    // 自动移除
    setTimeout(function() {
        notification.alert('close');
    }, 3000);
}

// 工具函数
function getStatusText(status) {
    const statusMap = {
        'QUEUED': '排队中',
        'RUNNING': '运行中',
        'FAILED': '失败',
        'COMPLETED': '完成'
    };
    return statusMap[status] || status;
}

function getSeverityText(severity) {
    const severityMap = {
        'LOW': '低',
        'MEDIUM': '中',
        'HIGH': '高',
        'CRITICAL': '严重'
    };
    return severityMap[severity] || severity;
}

function formatTime(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString('zh-CN');
}

function getTimeAgo(timestamp) {
    const now = new Date();
    const past = new Date(timestamp);
    const diffMs = now - past;
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return '刚刚';
    if (diffMins < 60) return `${diffMins}分钟前`;
    
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}小时前`;
    
    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays}天前`;
}